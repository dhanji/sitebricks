package com.google.sitebricks;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SitebricksAsyncModule extends SitebricksModule {
  public SitebricksAsyncModule() {
    // Prevents binding of various request-related stuff that we have Netty bindings for.
    enableServletSupport(false);
  }

  @Override
  protected final void configureSitebricks() {
    install(new SitebricksNettySupportModule());

    configureSitebricksAsync();
  }

  protected void configureSitebricksAsync() {}
}
