package com.google.sitebricks.cloud.mix;

import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class TestMix implements Mix {
  @Override
  public void mix(Map<String, Object> properties, Set<MavenDependency> deps) {
    deps.add(new MavenDependency("junit", "junit", "4.8.2", null, "test"));
    deps.add(new MavenDependency("org.mockito", "mockito-all", "1.9.0-rc1", null, "test"));
  }
}
