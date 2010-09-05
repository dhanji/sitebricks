package com.google.sitebricks.routing;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.sitebricks.Bricks;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Select;
import com.google.sitebricks.http.negotiate.ContentNegotiator;
import com.google.sitebricks.http.negotiate.Negotiation;
import com.google.sitebricks.rendering.EmbedAs;
import com.google.sitebricks.rendering.Strings;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * contains active uri/widget mappings
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe @Singleton
class DefaultPageBook implements PageBook {
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

  public Page serviceAt(String uri, Class<?> pageClass) {
    return at(uri, pageClass, true); 
  }

  public PageTuple at(String uri, Class<?> clazz) {
    return at(uri, clazz, clazz.isAnnotationPresent(Service.class));
  }

  private PageTuple at(String uri, Class<?> clazz, boolean headless) {
    final String key = firstPathElement(uri);
    final PageTuple pageTuple =
        new PageTuple(uri, new PathMatcherChain(uri), clazz, injector, headless);

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

  public Page embedAs(Class<?> clazz) {
    Preconditions.checkArgument(null == clazz.getAnnotation(Service.class),
        "You cannot embed headless web services!");
    String as = clazz.getAnnotation(EmbedAs.class).value();
    Strings.nonEmpty(as,
        "@EmbedAs() was empty. You must specify a valid widget name to embed as.");
    return embedAs(clazz, as);
  }

  public Page embedAs(Class<?> clazz, String as) {
    Preconditions.checkArgument(null == clazz.getAnnotation(Service.class),
        "You cannot embed headless web services!");
    PageTuple pageTuple = new PageTuple(null, PathMatcherChain.ignoring(), clazz, injector, false);

    synchronized (lock) {
      pagesByName.put(as.toLowerCase(), pageTuple);
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
      Class<?> superClass = aClass.getSuperclass();
      targetType = classToPageMap.get(superClass);

      // Stop at the root =D
      if (Object.class.equals(superClass)) {
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

    public Object doMethod(String httpMethod, Object page, String pathInfo, HttpServletRequest request) {
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

    public static InstanceBoundPage delegating(Page delegate, Object instance) {
      return new InstanceBoundPage(delegate, instance);
    }
  }

  @Select("") //the default select (hacky!!)
  public static class PageTuple implements Page {
    private final String uri;
    private final PathMatcher matcher;
    private final AtomicReference<Renderable> pageWidget = new AtomicReference<Renderable>();
    private final Class<?> clazz;
    private final boolean headless;
    private final Injector injector;

    private final Multimap<String, MethodTuple> methods;

    //dispatcher switch (select on request param by default)
    private final Select select;

    // A map of http methods -> annotation types (e.g. "POST" -> @Post)
    private Map<String, Class<? extends Annotation>> httpMethods;


    public PageTuple(String uri, PathMatcher matcher, Class<?> clazz, Injector injector, boolean headless) {

      this.uri = uri;
      this.matcher = matcher;
      this.clazz = clazz;
      this.injector = injector;
      this.headless = headless;

      this.select = discoverSelect(clazz);

      Key<Map<String, Class<? extends Annotation>>> methodMapKey =
              Key.get(new TypeLiteral<Map<String, Class<? extends Annotation>>>() {}, Bricks.class);

      this.httpMethods = injector.getInstance(methodMapKey);
      this.methods = reflectAndCache(httpMethods);
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
    private Multimap<String, MethodTuple> reflectAndCache(Map<String, Class<? extends Annotation>> methodMap) {
      Multimap<String, MethodTuple> map = HashMultimap.create();

      for (Map.Entry<String, Class<? extends Annotation>> entry : methodMap.entrySet()) {

          Class<? extends Annotation> get = entry.getValue();
          // First search any available public methods and store them (including inherited ones)
          for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(get)) {
              if (!method.isAccessible())
                method.setAccessible(true); //ugh

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
      return injector.getInstance(clazz);
    }

    public boolean isHeadless() {
      return headless;
    }

    public Object doMethod(String httpMethod, Object page, String pathInfo,
                           HttpServletRequest request) {

      //nothing to fire
      if (Strings.empty(httpMethod)) {
        return null;
      }

      @SuppressWarnings("unchecked")  // Guaranteed by javax.servlet
      Map<String, String[]> params = (Map<String, String[]>)request.getParameterMap();

      // Extract injectable pieces of the pathInfo.
      final Map<String, String> map = matcher.findMatches(pathInfo);

      //find method(s) to dispatch
      //  to
      final String[] events = params.get(select.value());
      if (null != events) {
        boolean matched = false;
        for (String event : events) {
          String key = httpMethod + event;
          Collection<MethodTuple> tuples = methods.get(key);
          Object redirect = null;
          
          if (null != tuples) {
            for (MethodTuple methodTuple : tuples) {
              if (methodTuple.shouldCall(request)) {
                matched = true;
                redirect = methodTuple.call(page, map);
                break;
              }
            }
          }

          //redirects interrupt the event dispatch sequence. Note this might cause inconsistent behaviour depending on
          // the order of processing for events.
          if (null != redirect) {
            return redirect;
          }
        }

        // no matched events. Fire default handler
        if (!matched) {
          return callMethodTuple(httpMethod, page, map, request);
        }

      } else {
        // Fire default handler (no events defined)
        return callMethodTuple(httpMethod, page, map, request);
      }

      //no redirects, render normally
      return null;
    }

    private Object callMethodTuple(String httpMethod, Object page, Map<String, String> pathMap,
                                   HttpServletRequest request) {

      // There may be more than one default handler
      Collection<MethodTuple> tuple = methods.get(httpMethod);
      Object redirect = null;
      if (null != tuple) {
        for (MethodTuple methodTuple : tuple) {
          if (methodTuple.shouldCall(request)) {
            redirect = methodTuple.call(page, pathMap);
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

      return this.clazz.equals(that.pageClass());
    }

    @Override
    public int hashCode() {
      return clazz.hashCode();
    }
  }


  private static class MethodTuple {
    private final Method method;
    private final List<String> args;
    private final Map<String, String> negotiates;
    private final ContentNegotiator negotiator;

    private MethodTuple(Method method, Injector injector) {
      this.method = method;
      this.args = reflect(method);
      this.negotiates = discoverNegotiates(method, injector);
      this.negotiator = injector.getInstance(ContentNegotiator.class);
    }

    private List<String> reflect(Method method) {
      final Annotation[][] annotationsGrid = method.getParameterAnnotations();
      if (null == annotationsGrid)
        return Collections.emptyList();

      List<String> args = new ArrayList<String>();
      for (Annotation[] annotations : annotationsGrid) {
        boolean namedFound = false;
        for (Annotation annotation : annotations) {
          if (Named.class.isInstance(annotation)) {
            Named named = (Named) annotation;

            args.add(named.value());
            namedFound = true;

            break;
          }
        }

        if (!namedFound)
          throw new InvalidEventHandlerException(
              "Encountered an argument not annotated with @Named in event handler method: " +
                  method);
      }

      return Collections.unmodifiableList(args);
    }

    /**
     * @return true if this method tuple can be validly called against this request.
     * Used to select for content negotiation.
     */
    public boolean shouldCall(HttpServletRequest request) {
      return negotiator.shouldCall(negotiates, request);
    }

    public Object call(Object page, Map<String, String> map) {
      List<String> arguments = new ArrayList<String>();
      for (String argName : args) {
        arguments.add(map.get(argName));
      }

      return call(page, method, arguments.toArray());
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

    private static Object call(Object page, final Method method,
                               Object[] args) {
      try {
        return method.invoke(page, args);
      } catch (IllegalAccessException e) {
        throw new EventDispatchException(
            "Could not access event method: " + method, e);
      } catch (InvocationTargetException e) {
        throw new EventDispatchException(
            "Event method threw an exception: " + method, e);
      }
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
         throw new IllegalStateException("Encountered a configured Negotiation annotation that " +
             "has no value parameter. This should never happen. " + annotation, e);
       } catch (InvocationTargetException e) {
         throw new IllegalStateException("Encountered a configured Negotiation annotation that " +
             "could not be read." + annotation, e);
       } catch (IllegalAccessException e) {
         throw new IllegalStateException("Encountered a configured Negotiation annotation that " +
             "could not be read." + annotation, e);
       }
     }

}
