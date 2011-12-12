package com.google.sitebricks.mail.webapp;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.sitebricks.SitebricksModule;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class WebConfig extends GuiceServletContextListener {
  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new SitebricksModule() {
      @Override
      protected void configureSitebricks() {
        scan(WebConfig.class.getPackage());
      }
    });
  }
}
