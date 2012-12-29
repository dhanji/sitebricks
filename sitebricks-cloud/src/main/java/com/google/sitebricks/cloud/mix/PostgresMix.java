package com.google.sitebricks.cloud.mix;

import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class PostgresMix implements Mix {
  @Override
  public void mix(Map<String, Object> properties, Set<MavenDependency> deps) {
    deps.add(new MavenDependency("postgresql", "postgresql", "9.1-901-1.jdbc4"));
  }
}
