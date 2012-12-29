package com.google.sitebricks.cloud.mix;

import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface Mix {
  void mix(Map<String, Object> properties, Set<MavenDependency> deps) throws Exception;
}
