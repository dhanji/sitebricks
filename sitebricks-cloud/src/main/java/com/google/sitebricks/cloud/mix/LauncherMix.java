package com.google.sitebricks.cloud.mix;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class LauncherMix implements Mix {

  @Override
  public void mix(Map<String, Object> properties, Set<MavenDependency> deps) throws Exception {
    File procfile = new File("Procfile");
    if (procfile.exists()) {
      LoggerFactory.getLogger("init").warn("Procfile exists. skipped.");
    } else {
      String main = properties.get("projectPackage").toString() + ".Main";
      FileWriter writer = new FileWriter(procfile);
      writer.write("web: java -cp target/classes:target/dependency/* " + main + "\n");
      writer.flush();
      writer.close();
    }
  }
}
