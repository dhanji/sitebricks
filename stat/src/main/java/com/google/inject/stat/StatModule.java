package com.google.inject.stat;

import com.google.common.base.Preconditions;
import com.google.inject.matcher.Matchers;
import com.google.inject.servlet.ServletModule;

/**
 * Module to install which enables statistics tracking and monitoring capabilities.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class StatModule extends ServletModule {
  private final String uriPath;

  public StatModule(String uriPath) {
    Preconditions.checkArgument(null != uriPath && !uriPath.isEmpty(),
        "URI path must be a valid non-empty servlet path mapping (example: /debug)");
    this.uriPath = uriPath;
  }

  @Override
  protected void configureServlets() {
    Stats stats = new Stats();

    bindListener(Matchers.any(), new StatAnnotatedTypeListener(stats));
    bind(Stats.class).toInstance(stats);

    serve(uriPath).with(StatsServlet.class);
  }
}
