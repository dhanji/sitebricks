package com.google.sitebricks;

import com.google.inject.servlet.ServletModule;

/**
 * Provides an optional mechanism for users of Sitebricks to supply {@link javax.servlet.Servlet} and
 * {@link javax.servlet.Filter} implementations using the standard Guice Servlet APIs.<br/>
 * <br/>
 * For example:
 * <pre>
  public Injector getInjector() {
    return Guice.createInjector(new SitebricksModule() {

      @Override
      protected SitebricksServletModule servletModule() {
        return new SitebricksServletModule() {

          @Override
          protected void configurePreFilters() {
            filter("/*").through(MyPreFilter.class);
          }

          @Override
          protected void configurePostFilters() {
            filter("/*").through(MyPostFilter.class);
          }

          @Override
          protected void configureCustomServlets() {
            serve("/foo").with(FooServlet.class);
          }
        };
      }


      @Override
      protected void configureSitebricks() {
        ...
      }
    }
 }
</pre>
 */
public class SitebricksServletModule extends ServletModule {

  @Override
  protected final void configureServlets() {
    configurePreFilters();

    filter("/*").through(HiddenMethodFilter.class);
    filter("/*").through(SitebricksFilter.class);

    configurePostFilters();
    configureCustomServlets();
  }

  /**
   * Provides a mechanism for users of Sitebricks to register their own {@link javax.servlet.Servlet} implementations
   * with Guice Servlet via {@link ServletModule#serve(String, String...) serve} and
   * {@link ServletModule#serveRegex(String, String...) serveRegex}.<p>
   */
  protected void configureCustomServlets() {
  }


  /**
   * Provides a mechanism for users of Sitebricks to register their own {@link javax.servlet.Filter} implementation with
   * Guice Servlet via {@link ServletModule#filter(String, String...) filter} and
   * {@link ServletModule#filterRegex(String, String...) filterRegex}.<p>
   * <br/>
   * Filters declared in this method will execute in the filter chain before the Sitebricks filter invokes.
   */
  protected void configurePreFilters() {
  }

  /**
   * Provides a mechanism for users of Sitebricks to register their own {@link javax.servlet.Filter} implementation with
   * Guice Servlet via {@link ServletModule#filter(String, String...) filter} and
   * {@link ServletModule#filterRegex(String, String...) filterRegex}.<p>
   * <br/>
   * Filters declared in this method will execute in the filter chain <b>only if Sitebricks determines it will not
   * handle the request.</b>
   */
  protected void configurePostFilters() {
  }

}
