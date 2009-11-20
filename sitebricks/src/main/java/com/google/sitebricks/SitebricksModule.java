package com.google.sitebricks;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.servlet.ServletModule;
import com.google.sitebricks.core.CaseWidget;
import com.google.sitebricks.http.Delete;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;
import com.google.sitebricks.rendering.Strings;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.RoutingDispatcher;
import org.mvel2.MVEL;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

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
    install(new ServletModule() {

      @Override
      protected void configureServlets() {
        filter("/*").through(HiddenMethodFilter.class);
        filter("/*").through(SitebricksFilter.class);
      }

    });

    // Call down to the implementation.
    configureSitebricks();

    //set up MVEL namespace (when jarjar-ed, it will use the repackaged namespace)
    System.setProperty("mvel.namespace",
        MVEL.class.getPackage().getName().replace('.', '/') + "/");

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

    //initialize startup services and routing modules
    install(PageBook.Routing.module());

    //development mode services
    if (Stage.DEVELOPMENT.equals(binder().currentStage())) {
      bind(PageBook.class).to(DebugModePageBook.class);
      bind(RoutingDispatcher.class).to(DebugModeRoutingDispatcher.class);
    }
  }

  protected void configureSitebricks() {
  }

  // Bindings.
  private final List<LinkingBinder> bindings = Lists.newArrayList();
  private final List<Package> packages = Lists.newArrayList();

  private final Map<String, Class<? extends Annotation>> methods = Maps.newHashMap();

  public final ShowBinder at(String uri) {
    LinkingBinder binding = new LinkingBinder(uri);
    bindings.add(binding);
    return binding;
  }

  public EmbedAsBinder embed(Class<?> clazz) {
    LinkingBinder binding = new LinkingBinder(clazz);
    bindings.add(binding);
    return binding;
  }

  public void bindMethod(String method, Class<? extends Annotation> annotation) {
    Strings.nonEmpty(method, "The REST method must be a valid non-empty string");
    Preconditions.checkArgument(null != annotation);

    String methodNormal = method.toLowerCase();
    methods.put(methodNormal, annotation);
  }

  protected final void scan(Package pack) {
    Preconditions.checkArgument(null != pack, "Package parameter to scan() cannot be null");
    packages.add(pack);
  }


  static enum BindingKind {
    EMBEDDED, PAGE, STATIC_RESOURCE
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
      this.pageClass = clazz;

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
