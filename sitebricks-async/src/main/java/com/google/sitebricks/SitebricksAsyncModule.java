package com.google.sitebricks;

import com.google.inject.util.Modules;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SitebricksAsyncModule extends SitebricksModule {
  @Override
  protected final void configureSitebricks() {
    install(Modules.override(new SitebricksServletSupportModule())
        .with(new SitebricksNettySupportModule()));

    configureSitebricksAsync();
  }

  protected void configureSitebricksAsync() {}
}
