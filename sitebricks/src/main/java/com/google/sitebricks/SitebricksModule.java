package com.google.sitebricks;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.sitebricks.core.CaseWidget;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import com.google.sitebricks.http.negotiate.Accept;
import com.google.sitebricks.http.negotiate.Negotiation;
import com.google.sitebricks.rendering.Strings;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SitebricksModule extends AbstractModule implements PageBinder {

  // Configure defaults via this contructor.
  public SitebricksModule() {
    // By default these are the method annotations we dispatch against.
    // They can be overridden with custom annotation types.
    methods.put("get", Get.class);
    methods.put("post", Post.class);
    methods.put("put", Put.class);
    methods.put("delete", Delete.class);
  }

  @Override
  protected final void configure() {

    // Re-route all requests through sitebricks.
    install(servletModule());
    install(new SitebricksInternalModule());

    // negotiations stuff (make sure we clean this up).
    negotiate("Accept").with(Accept.class);

    // Call down to the implementation.
    configureSitebricks();

    //insert core widgets set
    packages.add(0, CaseWidget.class.getPackage());

    bind(new TypeLiteral<List<Package>>() {
    })
        .annotatedWith(Bricks.class)
        .toInstance(packages);

    bind(new TypeLiteral<List<LinkingBinder>>() {
    })
        .annotatedWith(Bricks.class)
        .toInstance(bindings);

    // These are the HTTP methods that we listen for.
    bind(new TypeLiteral<Map<String, Class<? extends Annotation>>>() {
    })
        .annotatedWith(Bricks.class)
        .toInstance(methods);

    // These are Content negotiation annotations.
    bind(new TypeLiteral<Map<String, Class<? extends Annotation>>>() {
    })
        .annotatedWith(Negotiation.class)
        .toInstance(negotiations);

    Localizer.localizeAll(binder(), localizations);
  }

  /**
   * Optionally supply {@link javax.servlet.Servlet} and/or {@link javax.servlet.Filter} implementations to
   * Guice Servlet. See {@link com.google.sitebricks.SitebricksServletModule} for usage examples.
   *
   * @see com.google.sitebricks.SitebricksServletModule
   *
   * @return An instance of {@link com.google.sitebricks.SitebricksServletModule}. Implementing classes
   * must return a non-null value.
   */
  protected SitebricksServletModule servletModule() {
    return new SitebricksServletModule();
  }

  protected void configureSitebricks() {
  }

  // Bindings.
  private final List<LinkingBinder> bindings = Lists.newArrayList();
  private final List<Package> packages = Lists.newArrayList();

  private final Map<String, Class<? extends Annotation>> methods = Maps.newHashMap();
  private final Map<String, Class<? extends Annotation>> negotiations = Maps.newHashMap();
  private final Set<Localizer.Localization> localizations = Sets.newHashSet();

  public final ShowBinder at(String uri) {
    LinkingBinder binding = new LinkingBinder(uri);
    bindings.add(binding);
    return binding;
  }

  public final EmbedAsBinder embed(Class<?> clazz) {
    LinkingBinder binding = new LinkingBinder(clazz);
    bindings.add(binding);
    return binding;
  }

  public final void bindMethod(String method, Class<? extends Annotation> annotation) {
    Strings.nonEmpty(method, "The REST method must be a valid non-empty string");
    Preconditions.checkArgument(null != annotation);

    String methodNormal = method.toLowerCase();
    methods.put(methodNormal, annotation);
  }

  public NegotiateWithBinder negotiate(final String header) {
    Strings.nonEmpty(header, "invalid request header string for negotiation.");
    return new NegotiateWithBinder() {
      public void with(Class<? extends Annotation> ann) {
        Preconditions.checkArgument(null != ann);
        negotiations.put(header, ann);
      }
    };
  }

  public LocalizationBinder localize(final Class<?> iface) {
    Preconditions.checkArgument(iface.isInterface(), "localize() accepts an interface type only");
    localizations.add(Localizer.defaultLocalizationFor(iface));
    return new LocalizationBinder() {
      public void using(Locale locale, Map<String, String> messages) {
        localizations.add( new Localizer.Localization(iface, locale, messages));
      }

      public void using(Locale locale, Properties properties) {
        Preconditions.checkArgument(null != properties, "Must provide a non-null resource bundle");
        @SuppressWarnings("unchecked") // A Properties object is always of type string/string
        Map<String, String> messages = (Map) properties;
        localizations.add(new Localizer.Localization(iface, locale, messages));
      }

      public void using(Locale locale, ResourceBundle bundle) {
        Preconditions.checkArgument(null != bundle, "Must provide a non-null resource bundle");
        Map<String, String> messages = Maps.newHashMap();

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
          String key = keys.nextElement();
          messages.put(key, bundle.getString(key));
        }
        localizations.add(new Localizer.Localization(iface, locale, messages));
      }

      public void usingDefault() {
        localizations.add(Localizer.defaultLocalizationFor(iface));
      }
    };
  }

  protected final void scan(Package pack) {
    Preconditions.checkArgument(null != pack, "Package parameter to scan() cannot be null");
    packages.add(pack);
  }


  static enum BindingKind {
    EMBEDDED, PAGE, SERVICE, STATIC_RESOURCE
  }

  class LinkingBinder implements ShowBinder, ScopedBindingBuilder, EmbedAsBinder {
    BindingKind bindingKind;
    String embedAs;
    final String uri;
    Class<?> pageClass;
    private String resource;
    private boolean asEagerSingleton;
    private Class<? extends Annotation> scopeAnnotation;
    private Scope scope;


    public LinkingBinder(String uri) {
      this.uri = uri;
      this.pageClass = null;
      this.bindingKind = BindingKind.PAGE;
    }

    public LinkingBinder(Class<?> pageClass) {
      this.pageClass = pageClass;
      this.uri = null;
      this.bindingKind = BindingKind.EMBEDDED;
    }

    Export getResource() {
      return new Export() {
        public String at() {
          return uri;
        }

        public String resource() {
          return resource;
        }

        public Class<? extends Annotation> annotationType() {
          return Export.class;
        }
      };
    }

    public ScopedBindingBuilder show(Class<?> clazz) {
      Preconditions.checkArgument(!clazz.isAnnotationPresent(Service.class),
          "Cannot show() a headless web service. Did you mean to call serve() instead?");
      this.pageClass = clazz;
      this.bindingKind = BindingKind.PAGE;

      return this;
    }

    public ScopedBindingBuilder serve(Class<?> clazz) {
      Preconditions.checkArgument(!clazz.isAnnotationPresent(Show.class),
          "Cannot serve() a template page. Did you mean to call show() instead?");
      this.pageClass = clazz;
      this.bindingKind = BindingKind.SERVICE;

      return this;
    }

    public void export(String glob) {
      resource = glob;
      this.bindingKind = BindingKind.STATIC_RESOURCE;
    }

    public ScopedBindingBuilder as(String annotation) {
      this.embedAs = annotation;
      return this;
    }

    public void in(Class<? extends Annotation> scopeAnnotation) {
      Preconditions.checkArgument(null == scope);
      Preconditions.checkArgument(!asEagerSingleton);
      this.scopeAnnotation = scopeAnnotation;
    }

    public void in(Scope scope) {
      Preconditions.checkArgument(null == scopeAnnotation);
      Preconditions.checkArgument(!asEagerSingleton);
      this.scope = scope;
    }

    public void asEagerSingleton() {
      Preconditions.checkArgument(null == scopeAnnotation);
      Preconditions.checkArgument(null == scope);
      this.asEagerSingleton = true;
    }
  }
}
