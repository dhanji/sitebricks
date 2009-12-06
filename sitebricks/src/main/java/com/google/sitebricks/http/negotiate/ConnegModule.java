package com.google.sitebricks.http.negotiate;

import com.google.sitebricks.SitebricksModule;

/**
 *
 * Bindings for content negotiation defaults.
 */
public class ConnegModule extends SitebricksModule {

  @Override
  protected void configureSitebricks() {
    // NOTE(dhanji): Unused at the moment.
    negotiate("Accept").with(Accept.class);
  }
}
