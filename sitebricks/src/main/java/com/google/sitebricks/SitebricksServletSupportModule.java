package com.google.sitebricks;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.servlet.RequestScoped;
import com.google.sitebricks.headless.Request;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class SitebricksServletSupportModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Request.class).toProvider(ServletRequestProvider.class).in(RequestScoped.class);
  }

  @Provides
  @RequestScoped
  Locale provideLocale(HttpServletRequest request) {
    return request.getLocale();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof SitebricksServletSupportModule;
  }

  @Override
  public int hashCode() {
    return SitebricksServletSupportModule.class.hashCode();
  }
}
