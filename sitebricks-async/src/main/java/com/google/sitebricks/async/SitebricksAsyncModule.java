package com.google.sitebricks.async;

import com.google.inject.util.Modules;
import com.google.sitebricks.SitebricksModule;
import com.google.sitebricks.SitebricksServletSupportModule;

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
