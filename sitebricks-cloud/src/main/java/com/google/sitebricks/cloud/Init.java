package com.google.sitebricks.cloud;


import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class Init implements Command {
  private static volatile Logger log;

  @Override
  public void run(List<String> commands, Config config) throws Exception {
    log = LoggerFactory.getLogger("init");

    if (commands.size() < 2) {
      Cloud.quit("Usage: sitebricks init <project_name>");
    }

    File pomXml = new File("pom.xml");
    if (pomXml.exists()) {
      log.info("pom.xml already exists. cannot proceed");
      System.exit(1);
    }

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("packaging", "jar");
    properties.put("javaVersion", "1.6");
    properties.put("useMavenPaths", false);

    String projectName = commands.get(1);
    String group;
    System.out.println(commands);
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

    log.info("creating project structure");
    mkdir("src");
    mkdir("test");
    mkdir("resources");
    String packagePath = group.replace(".", "/") + '/' + projectName;
    mkdir("src/" + packagePath);
    mkdir("test/" + packagePath);

    properties.put("deps", new Mixin().run(commands, properties));
    String pom = TemplateRuntime.eval(
        Resources.toString(Init.class.getResource("pom.xml.mvel"), Charsets.UTF_8), properties)
        .toString();

    // Write new pom.xml
    log.info("writing pom.xml");
    final FileWriter fileWriter = new FileWriter("pom.xml");
    fileWriter.write(pom);
    fileWriter.flush();
    fileWriter.close();

    log.info("project initialized. Next, run 'sitebricks'");
  }

  private static void mkdir(String path) {
    File dir = new File(path);
    if (dir.exists()) {
      log.warn(path + " already exists. skipped.");
      return;
    }

    log.info(path + "/");
    if (!dir.mkdirs())
      Cloud.quit("IO error. Unable to mkdir " + path);
  }
}
