package com.google.sitebricks.cloud.mix;

import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MySqlMix implements Mix {
  @Override
  public void mix(Map<String, Object> properties, Set<MavenDependency> deps) {
    deps.add(new MavenDependency("mysql", "mysql-connector-java", "5.1.6"));
  }
}
