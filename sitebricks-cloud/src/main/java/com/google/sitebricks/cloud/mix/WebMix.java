package com.google.sitebricks.cloud.mix;

import com.google.sitebricks.cloud.Cloud;

import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class WebMix implements Mix {
  @Override
  public void mix(Map<String, Object> properties, Set<MavenDependency> deps) {
    deps.add(new MavenDependency("javax.servlet", "servlet-api", "2.5", null, "provided"));
    deps.add(new MavenDependency("com.google.sitebricks", "sitebricks", Cloud.SB_VERSION));
  }
}
