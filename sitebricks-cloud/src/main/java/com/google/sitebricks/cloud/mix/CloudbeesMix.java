package com.google.sitebricks.cloud.mix;

import java.util.Map;
import java.util.Set;

/**
 * Component mix for cloudbess maven plugin.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class CloudbeesMix implements Mix {
  @Override
  public void mix(Map<String, Object> properties, Set<MavenDependency> deps) throws Exception {
    MavenRepository repository = new MavenRepository("cloudbees-public-release",
        "http://repository-cloudbees.forge.cloudbees.com/public-release");
    repository.setType("pluginRepo");
    deps.add(repository);
    deps.add(new MavenPlugin("com.cloudbees", "bees-maven-plugin", "1.3.2"));
  }
}
