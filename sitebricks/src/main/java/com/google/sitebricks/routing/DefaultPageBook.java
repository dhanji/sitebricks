package com.google.sitebricks.routing;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.sitebricks.ActionDescriptor;
import com.google.sitebricks.At;
import com.google.sitebricks.Bricks;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.client.Transport;
import com.google.sitebricks.conversion.TypeConverter;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.As;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Head;
import com.google.sitebricks.http.Select;
import com.google.sitebricks.http.Trace;
import com.google.sitebricks.http.negotiate.ContentNegotiator;
import com.google.sitebricks.http.negotiate.Negotiation;
import com.google.sitebricks.rendering.Strings;
import com.google.sitebricks.rendering.control.DecorateWidget;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * contains active uri/widget mappings
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe @Singleton
public class DefaultPageBook implements PageBook {
  //multimaps TODO refactor to multimap?

  @GuardedBy("lock") // All three following fields
  private final Map<String, List<PageTuple>> pages = Maps.newHashMap();
  private final List<PageTuple> universalMatchingPages = Lists.newArrayList();
  private final Map<String, PageTuple> pagesByName = Maps.newHashMap();

  private final ConcurrentMap<Class<?>, PageTuple> classToPageMap =
      new MapMaker()
          .weakKeys()
          .weakValues()
          .makeMap();

  private final Object lock = new Object();
  private final Injector injector;

  @Inject
  public DefaultPageBook(Injector injector) {
    this.injector = injector;
  }

  @Override @SuppressWarnings("unchecked")
  public Collection<List<Page>> getPageMap() {
    return (Collection) pages.values();
  }

  // Page registration (internal) APIs
  public Page serviceAt(String uri, Class<?> pageClass) {
    // Handle subpaths, registering each as a separate instance of the page
    // tuple.
    for (Method method : pageClass.getDeclaredMethods()) {
      if (method.isAnnotationPresent(At.class)) {

        // This is a subpath expression.
        At at = method.getAnnotation(At.class);
        String subpath = at.value();

        // Validate subpath
        if (!subpath.startsWith("/") || subpath.isEmpty() || subpath.length() == 1) {
          throw new IllegalArgumentException(String.format(
              "Subpath At(\"%s\") on %s.%s() must begin with a \"/\" and must not be empty",
              subpath, pageClass.getName(), method.getName()));
        }

        subpath = uri + subpath;

        // Register as headless web service.
        at(subpath, pageClass, true);
      }
    }

    return at(uri, pageClass, true);
  }

  public PageTuple at(String uri, Class<?> clazz) {
    return at(uri, clazz, clazz.isAnnotationPresent(Service.class));
  }

  @Override
  public void at(String uri, List<ActionDescriptor> actionDescriptors,
                 Map<Class<? extends Annotation>, String> methodSet) {
    Multimap<String, Action> actions = HashMultimap.create();

    for (ActionDescriptor actionDescriptor : actionDescriptors) {
      for (Class<? extends Annotation> method : actionDescriptor.getMethods()) {
        String methodString = methodSet.get(method);
        Action action = actionDescriptor.getAction();

        if (null == action) {
          action = injector.getInstance(actionDescriptor.getActionKey());
        } else {
          injector.injectMembers(action);
        }

        actions.put(methodString, new SpiAction(action, actionDescriptor));
      }
    }

    // Register into the book!
    at(new PageTuple(uri, new PathMatcherChain(uri), null, true, false, injector, actions));
  }

  private void at(PageTuple page) {
    // Is Universal?
    synchronized (lock) {
      String key = firstPathElement(page.getUri());
      if (isVariable(key)) {
        universalMatchingPages.add(page);
      } else {
        multiput(pages, key, page);
      }
    }

    // Actions are not backed by classes.
    if (page.pageClass() != null)
      classToPageMap.put(page.pageClass(), page);
  }

  private PageTuple at(String uri, Class<?> clazz, boolean headless) {
    final String key = firstPathElement(uri);
    final PageTuple pageTuple =
        new PageTuple(uri, new PathMatcherChain(uri), clazz, injector, headless, false);

    synchronized (lock) {
      //is universal? (i.e. first element is a variable)
      if (isVariable(key))
        universalMatchingPages.add(pageTuple);
      else {
        multiput(pages, key, pageTuple);
      }
    }

    // Does not need to be inside lock, as it is concurrent.
    classToPageMap.put(clazz, pageTuple);

    return pageTuple;
  }

  public Page embedAs(Class<?> clazz, String as) {
    Preconditions.checkArgument(null == clazz.getAnnotation(Service.class),
        "You cannot embed headless web services!");
    PageTuple pageTuple = new PageTuple("", PathMatcherChain.ignoring(), clazz, injector, false, false);

    synchronized (lock) {
      pagesByName.put(as.toLowerCase(), pageTuple);
    }

    return pageTuple;
  }

  public Page decorate(Class<?> pageClass) {
    Preconditions.checkArgument(null == pageClass.getAnnotation(Service.class),
      "You cannot extend headless web services!");
    PageTuple pageTuple = new PageTuple("", PathMatcherChain.ignoring(), pageClass, injector, false, true);

    // store page with a special name used by DecorateWidget
    String name = DecorateWidget.embedNameFor(pageClass);
    synchronized (lock) {
      pagesByName.put(name, pageTuple);
    }

    return pageTuple;
  }

  public Page nonCompilingGet(String uri) {
    // The regular get is non compiling, in our case. So these methods are identical.
    return get(uri);
  }

  private static void multiput(Map<String, List<PageTuple>> pages, String key,
                               PageTuple page) {
    List<PageTuple> list = pages.get(key);

    if (null == list) {
      list = new ArrayList<PageTuple>();
      pages.put(key, list);
    }

    list.add(page);
  }

  private static boolean isVariable(String key) {
    return key.length() > 0 && ':' == key.charAt(0);
  }

  String firstPathElement(String uri) {
    String shortUri = uri.substring(1);

    final int index = shortUri.indexOf("/");

    return (index >= 0) ? shortUri.substring(0, index) : shortUri;
  }

  @Nullable
  public Page get(String uri) {
    final String key = firstPathElement(uri);

    List<PageTuple> tuple = pages.get(key);

    //first try static first piece
    if (null != tuple) {

      //first try static first piece
      for (PageTuple pageTuple : tuple) {
        if (pageTuple.matcher.matches(uri))
          return pageTuple;
      }
    }

    //now try dynamic first piece (how can we make this faster?)
    for (PageTuple pageTuple : universalMatchingPages) {
      if (pageTuple.matcher.matches(uri))
        return pageTuple;
    }

    //nothing matched
    return null;
  }

  public Page forName(String name) {
    return pagesByName.get(name);
  }

  @Nullable
  public Page forInstance(Object instance) {
    Class<?> aClass = instance.getClass();
    PageTuple targetType = classToPageMap.get(aClass);

    // Do a super crawl to detect the target type.
    while (null == targetType) {
      aClass = aClass.getSuperclass();
      targetType = classToPageMap.get(aClass);

      // Stop at the root =D
      if (Object.class.equals(aClass)) {
        return null;
      }
    }

    return InstanceBoundPage.delegating(targetType, instance);
  }

  public Page forClass(Class<?> pageClass) {
    return classToPageMap.get(pageClass);
  }

  public static class InstanceBoundPage implements Page {
    private final Page delegate;
    private final Object instance;

    private InstanceBoundPage(Page delegate, Object instance) {
      this.delegate = delegate;
      this.instance = instance;
    }

    public Renderable widget() {
      return delegate.widget();
    }

    public Object instantiate() {
      return instance;
    }

    public Object doMethod(String httpMethod, Object page, String pathInfo, Request request)
        throws IOException {
      return delegate.doMethod(httpMethod, page, pathInfo, request);
    }

    public Class<?> pageClass() {
      return delegate.pageClass();
    }

    public void apply(Renderable widget) {
      delegate.apply(widget);
    }

    public String getUri() {
      return delegate.getUri();
    }

    public boolean isHeadless() {
      return delegate.isHeadless();
    }

    @Override
    public boolean isDecorated() {
      return delegate.isDecorated();
    }

    public Set<String> getMethod() {
      return delegate.getMethod();
    }

    public int compareTo(Page page) {
      return delegate.compareTo(page);
    }

    public static InstanceBoundPage delegating(Page delegate, Object instance) {
      return new InstanceBoundPage(delegate, instance);
    }

    @Override
    public Multimap<String, Action> getMethods() {
      return delegate.getMethods();
    }
  }

  @Select("") //the default select (hacky!!)
  public static class PageTuple implements Page {
    private final String uri;
    private final PathMatcher matcher;
    private final AtomicReference<Renderable> pageWidget = new AtomicReference<Renderable>();
    private final Class<?> clazz;
    private final boolean headless;
    private final boolean extension;
    private final Injector injector;

    private final Multimap<String, Action> methods;

    //dispatcher switch (select on request param by default)
    private final Select select;
    private static final Key<Map<String, Class<? extends Annotation>>> HTTP_METHODS_KEY =
        Key.get(new TypeLiteral<Map<String, Class<? extends Annotation>>>() {}, Bricks.class);

    // A map of http methods -> annotation types (e.g. "POST" -> @Post)
    private Map<String, Class<? extends Annotation>> httpMethods;

    public PageTuple(String uri, PathMatcher matcher, Class<?> clazz, boolean headless, boolean extension,
                     Injector injector, Multimap<String, Action> methods) {
      this.uri = uri;
      this.matcher = matcher;
      this.clazz = clazz;
      this.headless = headless;
      this.extension = extension;
      this.injector = injector;
      this.methods = methods;
      this.select = PageTuple.class.getAnnotation(Select.class);
      this.httpMethods = injector.getInstance(HTTP_METHODS_KEY);
    }

    public PageTuple(String uri, PathMatcher matcher, Class<?> clazz, Injector injector,
                     boolean headless, boolean extension) {
      this.uri = uri;
      this.matcher = matcher;
      this.clazz = clazz;
      this.injector = injector;
      this.headless = headless;
      this.extension = extension;

      this.select = discoverSelect(clazz);

      this.httpMethods = injector.getInstance(HTTP_METHODS_KEY);
      this.methods = reflectAndCache(uri, httpMethods);
    }

    //the @Select request parameter-based event dispatcher
    private Select discoverSelect(Class<?> clazz) {
      final Select select = clazz.getAnnotation(Select.class);
      if (null != select)
        return select;
      else
        return PageTuple.class.getAnnotation(Select.class);
    }

    /**
     * Returns a map of HTTP-method name to @Annotation-marked methods
     */
    @SuppressWarnings({"JavaDoc"})
    private Multimap<String, Action> reflectAndCache(String uri,
        Map<String, Class<? extends Annotation>> methodMap) {
      String tail = "";
      if (clazz.isAnnotationPresent(At.class)) {
        int length = clazz.getAnnotation(At.class).value().length();

        // It's possible that the uri being registered is shorter than the
        // class length, this can happen in the case of using the .at() module
        // directive to override @At() URI path mapping. In this case we treat
        // this call as a top-level path registration with no tail. Any
        // encountered subpath @At methods will be ignored for this URI.
        if (uri != null && length <= uri.length())
          tail = uri.substring(length);
      }

      Multimap<String, Action> map = HashMultimap.create();

      for (Map.Entry<String, Class<? extends Annotation>> entry : methodMap.entrySet()) {

          Class<? extends Annotation> get = entry.getValue();
          // First search any available public methods and store them (including inherited ones)
          for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(get)) {
              if (!method.isAccessible())
                method.setAccessible(true); //ugh

              // Be defensive about subpaths.
              if (method.isAnnotationPresent(At.class)) {
                // Skip any at-annotated methods for a top-level path registration.
                if (tail.isEmpty()) {
                  continue;
                }

                // Skip any at-annotated methods that do not exactly match the path.
                if (!tail.equals(method.getAnnotation(At.class).value())) {
                  continue;
                }
              } else if (!tail.isEmpty()) {
                // If this is the top-level method we're scanning, but their is a tail, i.e.
                // this is not intended to be served by the top-level method, then skip.
                continue;
              }

              // Otherwise register this method for firing...

              //remember default value is empty string
              String value = getValue(get, method);
              String key = (Strings.empty(value)) ? entry.getKey() : entry.getKey() + value;
              map.put(key, new MethodTuple(method, injector));
            }
          }

          // Then search class's declared methods only (these take precedence)
          for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(get)) {
              if (!method.isAccessible())
                method.setAccessible(true); //ugh

              // Be defensive about subpaths.
              if (method.isAnnotationPresent(At.class)) {
                // Skip any at-annotated methods for a top-level path registration.
                if (tail.isEmpty()) {
                  continue;
                }

                // Skip any at-annotated methods that do not exactly match the path.
                if (!tail.equals(method.getAnnotation(At.class).value())) {
                  continue;
                }
              } else if (!tail.isEmpty()) {
                // If this is the top-level method we're scanning, but their is a tail, i.e.
                // this is not intended to be served by the top-level method, then skip.
                continue;
              }

              // Otherwise register this method for firing...

              //remember default value is empty string
              String value = getValue(get, method);
              String key = (Strings.empty(value)) ? entry.getKey() : entry.getKey() + value;
              map.put(key, new MethodTuple(method, injector));
            }
          }
      }

      return map;
    }

    private String getValue(Class<? extends Annotation> get, Method method) {
      return readAnnotationValue(method.getAnnotation(get));
    }

    public Renderable widget() {
      return pageWidget.get();
    }

    public Object instantiate() {
      return clazz == null ? Collections.emptyMap() : injector.getInstance(clazz);
    }

    public boolean isHeadless() {
      return headless;
    }

    @Override
    public boolean isDecorated() {
      return extension;
    }

    public Set<String> getMethod() {
      return methods.keySet();
    }

    public int compareTo(Page page) {
      return uri.compareTo(page.getUri());
    }

    public Object doMethod(String httpMethod, Object page, String pathInfo,
                           Request request) throws IOException {

      //nothing to fire
      if (Strings.empty(httpMethod)) {
        return null;
      }

      // NOTE(dhanji): This slurps the entire Map. It could potentially be optimized...
      Multimap<String, String> params = request.params();

      // Extract injectable pieces of the pathInfo.
      final Map<String, String> map = matcher.findMatches(pathInfo);

      // Find method(s) to dispatch to.
      Collection<String> events = params.get(select.value());
      if (null != events) {
        boolean matched = false;
        for (String event : events) {
          String key = httpMethod + event;
          Collection<Action> tuples = methods.get(key);
          Object redirect = null;

          if (null != tuples) {
            for (Action action : tuples) {
              if (action.shouldCall(request)) {
                matched = true;
                redirect = action.call(request, page, map);
                break;
              }
            }
          }

          // Redirects interrupt the event dispatch sequence. Note this might cause inconsistent
          // behaviour depending on the order of processing for events.
          if (null != redirect) {
            return redirect;
          }
        }

        // no matched events. Fire default handler
        if (!matched) {
          return callAction(httpMethod, page, map, request);
        }

      } else {
        // Fire default handler (no events defined)
        return callAction(httpMethod, page, map, request);
      }

      //no redirects, render normally
      return null;
    }

    private Object callAction(String httpMethod, Object page, Map<String, String> pathMap,
                              Request request) throws IOException {

      // There may be more than one default handler
      Collection<Action> tuple = methods.get(httpMethod);
      Object redirect = null;
      if (null != tuple) {
        for (Action action : tuple) {
          if (action.shouldCall(request)) {
            redirect = action.call(request, page, pathMap);
            break;
          }
        }
      }
      return redirect;
    }

    public Class<?> pageClass() {
      return clazz;
    }

    public void apply(Renderable widget) {
      this.pageWidget.set(widget);
    }

    public String getUri() {
      return uri;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Page)) return false;

      Page that = (Page) o;

      return this.clazz.equals(that.pageClass()) && isDecorated() == that.isDecorated();
    }

    @Override
    public int hashCode() {
      return clazz.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(PageTuple.class).add("clazz", clazz).add("isDecorated", extension)
                .add("uri", uri).toString();
    }

    @Override
    public Multimap<String, Action> getMethods() {
      return methods;
    }
  }

  public static class MethodTuple implements Action {
    private final Method method;
    private final Injector injector;
    private final List<Object> args;
    private final Map<String, String> negotiates;
    private final ContentNegotiator negotiator;
	  private final TypeConverter converter;
    private final As returnAs;

    private MethodTuple(Method method, Injector injector) {
      this.method = method;
      this.injector = injector;
      this.args = reflect(method);
      this.negotiates = discoverNegotiates(method, injector);
      this.negotiator = injector.getInstance(ContentNegotiator.class);
      this.converter = injector.getInstance(TypeConverter.class);
      this.returnAs = method.getAnnotation(As.class);
    }

    public Method getMethod() {
      return method;
    }
    
    private List<Object> reflect(Method method) {
      final Annotation[][] annotationsGrid = method.getParameterAnnotations();
      if (null == annotationsGrid)
        return Collections.emptyList();

      List<Object> args = new ArrayList<Object>();
      for (int i = 0; i < annotationsGrid.length; i++) {
        Annotation[] annotations = annotationsGrid[i];

        Annotation bindingAnnotation = null;
        boolean preInjectableFound = false;
        for (Annotation annotation : annotations) {
          if (Named.class.isInstance(annotation)) {
            Named named = (Named) annotation;

			      args.add(new NamedParameter(named.value(), method.getGenericParameterTypes()[i]));
            preInjectableFound = true;

            break;
          } else if (annotation.annotationType().isAnnotationPresent(BindingAnnotation.class)) {
            bindingAnnotation = annotation;
          } else if (As.class.isInstance(annotation)) {
            As as = (As) annotation;
            if (method.isAnnotationPresent(Get.class)
                || method.isAnnotationPresent(Head.class)
                || method.isAnnotationPresent(Trace.class))
              throw new IllegalArgumentException("Cannot accept a @As(...) request body from" +
                  " method marked @Get, @Head or @Trace: "
                  + method.getDeclaringClass().getName() + "#" + method.getName() + "()");

            preInjectableFound = true;
            args.add(new AsParameter(as.value(), TypeLiteral.get(method.getGenericParameterTypes()[i])));
            break;
          }
        }

        if (!preInjectableFound) {
          // Could be an arbitrary injection request.
          Class<?> argType = method.getParameterTypes()[i];
          Key<?> key = (null != bindingAnnotation)
              ? Key.get(argType, bindingAnnotation)
              : Key.get(argType);

          args.add(key);

          if (null == injector.getBindings().get(key))
            throw new InvalidEventHandlerException(
                "Encountered an argument not annotated with @Named and not a valid injection key"
                + " in event handler method: " + method + " " + key);
        }

      }

      return Collections.unmodifiableList(args);
    }

    /**
     * @return true if this method tuple can be validly called against this request.
     * Used to select for content negotiation.
     */
    @Override
    public boolean shouldCall(Request request) {
      return negotiator.shouldCall(negotiates, request);
    }


    @Override
    public Object call(Request request, Object page, Map<String, String> map) throws IOException {
      List<Object> arguments = new ArrayList<Object>();
      for (Object arg : args) {
        if (arg instanceof AsParameter) {
          AsParameter as = (AsParameter) arg;
          arguments.add(request.read(as.type).as(as.transport));
        } else if (arg instanceof NamedParameter) {
          NamedParameter np = (NamedParameter) arg;
          String text = map.get(np.getName());
          Object value = converter.convert(text, np.getType());
          arguments.add(value);
        } else
          arguments.add(injector.getInstance((Key<?>) arg));
      }

      Object result = call(page, method, arguments.toArray());
      if (returnAs != null && result instanceof Reply) {
        ((Reply) result).as(returnAs.value());
      }
      return result;
    }

    private static Object call(Object page, final Method method,
                               Object[] args) {
      try {
        return method.invoke(page, args);
      } catch (IllegalAccessException e) {
        throw new EventDispatchException(
            "Could not access event method (appears to be a security problem): " + method, e);
      } catch (InvocationTargetException e) {
        Throwable cause = e.getCause();
        StackTraceElement[] stackTrace = cause.getStackTrace();
        throw new EventDispatchException(String.format(
            "Exception [%s - \"%s\"] thrown by event method [%s]\n\nat %s\n"
            + "(See below for entire trace.)\n",
            cause.getClass().getSimpleName(),
            cause.getMessage(), method,
            stackTrace[0]), e);
      }
    }

      //the @Accept request header-based event dispatcher
    private Map<String, String> discoverNegotiates(Method method, Injector injector) {
      // This ugly gunk gets us the map of headers to negotiation annotations
      Map<String, Class<? extends Annotation>> negotiationsMap = injector.getInstance(
          Key.get(new TypeLiteral<Map<String, Class<? extends Annotation>>>(){ }, Negotiation.class));

      Map<String, String> negotiations = Maps.newHashMap();
      // Gather all the negotiation annotations in this class.
      for (Map.Entry<String, Class<? extends Annotation>> headerAnn : negotiationsMap.entrySet()) {
        Annotation annotation = method.getAnnotation(headerAnn.getValue());
        if (annotation != null) {
          negotiations.put(headerAnn.getKey(), readAnnotationValue(annotation));
        }
      }

      return negotiations;
    }

    public class NamedParameter {
      private final String name;
      private final Type type;

      public NamedParameter(String name, Type type) {
        this.name = name;
        this.type = type;
      }

      public String getName() {
        return name;
      }

      public Type getType() {
        return type;
      }
    }

    public class AsParameter {
      private final Class<? extends Transport> transport;
      private final TypeLiteral<?> type;

      public AsParameter(Class<? extends Transport> transport, TypeLiteral<?> type) {
        this.transport = transport;
        this.type = type;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MethodTuple that = (MethodTuple) o;

      if (!method.equals(that.method)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      return method.hashCode();
    }
  }

  /**
   * A simple utility method that reads the String value attribute of any annotation
   * instance.
   */
  static String readAnnotationValue(Annotation annotation) {
       try {
         Method m = annotation.getClass().getMethod("value");

         return (String) m.invoke(annotation);

       } catch (NoSuchMethodException e) {
         throw new IllegalStateException("Encountered a configured annotation that " +
             "has no value parameter. This should never happen. " + annotation, e);
       } catch (InvocationTargetException e) {
         throw new IllegalStateException("Encountered a configured annotation that " +
             "could not be read." + annotation, e);
       } catch (IllegalAccessException e) {
         throw new IllegalStateException("Encountered a configured annotation that " +
             "could not be read." + annotation, e);
       }
     }

}
