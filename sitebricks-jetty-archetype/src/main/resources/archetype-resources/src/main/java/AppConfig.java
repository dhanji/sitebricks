package ${package};

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;
import ${package}.web.HomePage;

/**
 * The main configuration for a sitebricks servlet app. This class is typically
 * registered as a &lt;listener&gt; in web.xml.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class AppConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new SitebricksModule() {
      @Override
      protected void configureSitebricks() {

        // Tell Sitebricks to scan the contents of package info.sitebricks.example.web
        // and ALL of its child packages for bricks, pages and other sitebricks artifacts.
        scan(HomePage.class.getPackage());

      }
    });
  }
}
