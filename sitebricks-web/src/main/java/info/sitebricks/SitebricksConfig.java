package info.sitebricks;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;
import info.sitebricks.web.Home;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SitebricksConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new SitebricksModule() {
      @Override
      protected void configureSitebricks() {
        scan(Home.class.getPackage());
      }
    });
  }
}
