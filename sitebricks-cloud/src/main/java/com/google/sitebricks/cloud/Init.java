package com.google.sitebricks.cloud;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Init implements Command {
  private static volatile Logger log;

  @Override
  public void run(List<String> commands, Config config) throws Exception {
    log = LoggerFactory.getLogger("init");

    if (commands.size() < 2) {
      Cloud.quit("Usage: sitebricks init <project_name>");
    }

    File pomXml = new File("pom.xml");
    if (pomXml.exists() && !config.force()) {
      log.info("pom.xml already exists. cannot proceed");
      System.exit(1);
    }

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("packaging", "jar");
    properties.put("javaVersion", "1.6");
    properties.put("useMavenPaths", false);

    String projectName = commands.get(1);
    String group;
    if (projectName.contains(":")) {
      String[] split = projectName.split(":");
      if (split.length < 3)
        Cloud.quit("Malformed project id (must be groupId:artifactId:version): " + projectName);

      group = split[0];
      properties.put("projectGroup", group);
      properties.put("projectName", projectName = split[1]);
      properties.put("projectVersion", split[2]);
    } else {
      properties.put("projectGroup", group = "org.sitebricks");
      properties.put("projectName", projectName);
      properties.put("projectVersion", "1.0");
    }

    String cleanedProjectName = projectName.replaceAll("[-.:]", "");
    properties.put("cleanedProjectName", cleanedProjectName);

    log.info("creating project structure");
    Cloud.mkdir("config");
    Cloud.mkdir("src");
    Cloud.mkdir("test");
    Cloud.mkdir("resources");
    String packagePath = group.replace(".", "/") + '/' + cleanedProjectName;
    Cloud.mkdir("src/" + packagePath);
    Cloud.mkdir("test/" + packagePath);

    properties.put("packagePath", packagePath);
    properties.put("projectPackage", group + '.' + cleanedProjectName);

    properties.put("deps", new Mixin().run(commands, properties));
    // Write new pom.xml
    Cloud.writeTemplate("pom.xml", properties);
    Cloud.writeTemplate("environment.yml", "config/environment.yml", properties);
    Cloud.writeTemplate("logback.xml", "resources/logback.xml", properties);
    log.info("project initialized. Next, run 'sitebricks'");
  }
}
