package com.google.sitebricks.cloud.mix;

import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class JettyMix implements Mix {
  private static final String JETTY_VERSION = "8.1.6.v20120903";

  @Override
  public void mix(Map<String, Object> properties, Set<MavenDependency> deps) {
    deps.add(new MavenDependency("org.eclipse.jetty", "jetty-server", JETTY_VERSION));
    deps.add(new MavenDependency("org.eclipse.jetty", "jetty-webapp", JETTY_VERSION));
  }
}
