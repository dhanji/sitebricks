package com.google.sitebricks.routing;

import com.google.inject.AbstractModule;
import com.google.inject.ImplementedBy;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.sitebricks.ActionDescriptor;
import com.google.sitebricks.Renderable;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(DefaultPageBook.class)
public interface PageBook {

  /**
   * Register a page class at the given contextual URI.
   *
   * @return A {@link Page} representing the given class
   *  without a compiled template applied.
   */
  Page at(String uri, Class<?> myPageClass);

  /**
   *
   * @param uri A contextual URI where a page (maybe) registered.
   * @return A {@link Page} object thatis capable of rend
   */
  Page get(String uri);

  Page forName(String name);

  /**
   * Registers a page class as an embeddable.
   * @param as The annotation name to register this widget as.
   *  Example: {@code "Hello"} will make this page class
   * available for embedding as <pre>{@literal @}Hello</pre>.
   */
  Page embedAs(Class<?> pageClass, String as);
  
  /**
   * Indicates that the template for this class is to be
   * inserted into a superclass template using @Decorated
   * 
   * @param pageClass
   */
  Page decorate(Class<?> pageClass);

  /**
   * Same as {@linkplain #get} except guaranteed not to trigger a
   * cascading compile of page bricks.
   */
  Page nonCompilingGet(String uri);

  /**
   * Similar to {@linkplain #get} except that instead of returning
   * a page for a URI, it returns the page matching the class of the
   * provided instance, and uses the instance itself to deliver the
   * page.
   *
   * @param instance An instance of some page registered by an {@literal @}{@code At}
   * annotation or similar method in this sitebricks app.
   */
  Page forInstance(Object instance);

  /**
   * Very similar to {@linkplain #forInstance(Object)}, except that it
   * takes a class literal instead and does NOT do super crawling.
   */
  Page forClass(Class<?> pageClass);

  /**
   * Same as {@linkplain #at} but registers a headless web service instead.
   */
  Page serviceAt(String uri, Class<?> pageClass);

  Collection<List<Page>> getPageMap();

  void at(String uri, List<ActionDescriptor> actionDescriptor,
          Map<Class<? extends Annotation>, String> methodSet);


  public static interface Page extends Comparable<Page> {
    Renderable widget();

    Object instantiate();

    Object doMethod(String httpMethod, Object page, String pathInfo, HttpServletRequest request);

    Class<?> pageClass();

    void apply(Renderable widget);

    String getUri();

    boolean isHeadless();

    boolean isDecorated();

    Set<String> getMethod();
  }

  public static final class Routing extends AbstractModule {
    private Routing() {
    }

    @Override
    protected final void configure() {
      if (Stage.DEVELOPMENT.equals(binder().currentStage())) {
        bind(PageBook.class)
            .annotatedWith(Production.class)
            .to(DefaultPageBook.class);

        bind(RoutingDispatcher.class)
            .annotatedWith(Production.class)
            .to(WidgetRoutingDispatcher.class);
      }
    }

    public static Module module() {
      return new Routing();
    }
    
    //Ensures only one instance of the Routine module is installed.
    @Override
    public boolean equals(Object obj) {
      return obj instanceof Routing;
    }

    @Override
    public int hashCode() {
      return Routing.class.hashCode();
    }
  }
}
