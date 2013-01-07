package com.google.sitebricks.cloud.mix;

import com.google.sitebricks.cloud.Cloud;

import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MailMix implements Mix {
  @Override
  public void mix(Map<String, Object> properties, Set<MavenDependency> deps) {
    deps.add(new MavenDependency("com.google.sitebricks", "sitebricks-mail", Cloud.SB_VERSION));
  }
}
