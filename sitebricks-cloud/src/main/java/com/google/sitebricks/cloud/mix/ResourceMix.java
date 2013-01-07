package com.google.sitebricks.cloud.mix;

import com.google.sitebricks.cloud.Cloud;
import com.google.sitebricks.cloud.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Mix an individual webservice.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class ResourceMix implements Mix {
  private static final Logger log = LoggerFactory.getLogger("@resource");

  @Inject
  private Config config;

  @Override
  public void mix(Map<String, Object> properties, Set<MavenDependency> deps) throws Exception {
    mixResource(properties, config);
  }

  private void mixResource(Map<String, Object> properties, Config config) throws IOException {
    String packagePath = properties.get("packagePath").toString();

    if (config.name() == null) {
      Cloud.quit("Expected name argument. Example:" +
          "\nsitebricks mix @resource --name=Person\n" +
          "\nsitebricks mix @resource --at=/people --name=Person" +
          "\nsitebricks mix @resource --name=persons.Person\n"
      );
    }

    if (config.name().contains(".")) {
      String dir = config.name().replace('.', '/');
      dir = dir.substring(0, dir.lastIndexOf('.'));
      Cloud.mkdir(dir);
      properties.put("dir", dir);
    }

    String at = config.at() == null ? "/" + config.name().toLowerCase().replace('.', '/') : config.at() + ".";
    properties.put("name", config.name());
    properties.put("at", at);
    properties.put("isTemplate", config.show() != null);
    properties.put("show", config.show());
    String to = "src/" + packagePath + "/web/" + config.name().replace('.', '/') + ".java";
    if (new File(to).exists()) {
      if (config.force())
        log.info("{} already exists. overwriting. (--force active)", to);
      else
        log.info("{} already exists. please try again.", to);
    } else
      Cloud.writeTemplate("WebService.java", to, properties);
  }
}
