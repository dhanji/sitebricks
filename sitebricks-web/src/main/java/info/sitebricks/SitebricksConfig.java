package info.sitebricks;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.sitebricks.SitebricksModule;
import info.sitebricks.persist.PersistFilter;
import info.sitebricks.persist.StoreModule;
import info.sitebricks.web.WikiService;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SitebricksConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new ServletModule() {
      @Override
      protected void configureServlets() {
        install(new StoreModule());
        filter("/*").through(PersistFilter.class);
      }

    }, new SitebricksModule() {
      @Override
      protected void configureSitebricks() {
        scan(WikiService.class.getPackage());
      }
    });
  }
}
