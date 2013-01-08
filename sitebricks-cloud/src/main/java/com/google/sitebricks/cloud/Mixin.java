package com.google.sitebricks.cloud;

import com.google.common.io.CharStreams;
import com.google.sitebricks.cloud.mix.MavenDependency;
import com.google.sitebricks.cloud.mix.Mixes;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Mixin implements Command {
  private static final Logger log = LoggerFactory.getLogger("mixer");

  @Override
  @SuppressWarnings("unchecked")
  public void run(List<String> commands, Config config) throws Exception {
    if (commands.size() < 2) {
      Cloud.quit("Usage: sitebricks mix @mix1 @mix2 ...");
    }

    File pomXml = new File("pom.xml");
    if (!pomXml.exists()) {
      Cloud.quit("Missing pom.xml. cannot proceed.");
    }

    Document document = DocumentHelper.parseText(CharStreams.toString(new FileReader(pomXml)));

    Map<String, Object> properties = new HashMap<String, Object>();
    properties.put("javaVersion", "1.6");
    properties.put("useMavenPaths", false);

    List<Node> list = document.selectNodes("/project/groupId");
    properties.put("projectGroup", list.get(0).getText());

    list = document.selectNodes("/project/artifactId");
    properties.put("projectName", list.get(0).getText());

    list = document.selectNodes("/project/version");
    properties.put("projectVersion", list.get(0).getText());

    list = document.selectNodes("/project/packaging");
    properties.put("packaging", list.get(0).getText());

    list = document.selectNodes("/project/build/plugins/plugin");
    if (!list.isEmpty()) {
      for (Node node : list) {
        List<Node> plugin = node.selectNodes("artifactId");
        if ("maven-compiler-plugin".equals(plugin.get(0).getText())) {
          plugin = node.selectNodes("configuration/source");
          if (!plugin.isEmpty())
            properties.put("javaVersion", plugin.get(0).getText());
        }
      }
      properties.put("packaging", list.get(0).getText());
    }

    list = document.selectNodes("/project/build/sourceDirectory");
    if (list.isEmpty() || "src/main/java".equals(list.get(0).getText())) {
      properties.put("useMavenPaths", true);
    }

    Set<MavenDependency> deps = run(commands, properties);

    // Write deps back into the POM if necessary.
    list = document.selectNodes("/project/dependencies/dependency");
    for (Node node : list) {
      MavenDependency existing = new MavenDependency(
          ((Node) node.selectNodes("groupId").get(0)).getText(),
          ((Node) node.selectNodes("artifactId").get(0)).getText(),
          ((Node) node.selectNodes("version").get(0)).getText());
      deps.remove(existing);
    }

    // Then we have new deps to write!
    if (!deps.isEmpty()) {
      for (MavenDependency dep : deps) {
        list.add(DocumentHelper.parseText(dep.toDepString()));
      }

      log.info("updating pom.xml with new deps");
      FileWriter fileWriter = new FileWriter(pomXml);
      fileWriter.write(document.asXML());
      fileWriter.flush();
      fileWriter.close();
    }
  }

  public Set<MavenDependency> run(List<String> commands, Map<String, Object> properties) throws Exception {
    // Run mixes first.
    Collection<String> mixes = new ArrayList<String>();
    for (String command : commands) {
      if (command.startsWith("@"))
        mixes.add(command);
    }

    if (mixes.isEmpty())
      mixes = Mixes.DEFAULT_MIXES;

    Set<MavenDependency> deps = new LinkedHashSet<MavenDependency>();
    for (String mix : mixes) {
      log.info("adding in " + mix);
      Mixes.get(mix).mix(properties, deps);
    }

    return deps;
  }
}
