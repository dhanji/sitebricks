package com.google.sitebricks.cloud;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.sitebricks.cloud.proc.DynamicCompilation;
import com.google.sitebricks.cloud.proc.Proc;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.xpath.DefaultXPath;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class ProcRunner implements Command {
  private static final Map<String, String> POM_NAMESPACE;
  static {
    POM_NAMESPACE = new HashMap<String, String>();
    POM_NAMESPACE.put("m", "http://maven.apache.org/POM/4.0.0");
  }

  @Override
  public void run(List<String> commands, Config config) throws Exception {
    List<Proc> procs = readProcs(config);

    // Compile project first, if necessary.
    DynamicCompilation.compile(config);

    // Load environment config.
    String envName = System.getenv("ENV");
    if (envName == null)
      envName = "local";

    Map<String, String> env = readEnvironment(envName);
    if (env == null) {
      Cloud.quit("unknown environment: " + envName);
    }

    // Default to log level info for our app.
    assert env != null;
    if (env.get("loglevel") == null)
      env.put("loglevel", "info");

    // Start procs.
    for (Proc proc : procs) {
      proc.start(toEnvironmentArray(env));
    }
    LoggerFactory.getLogger("sitebricks").info("all jobs started");

    for (Proc proc : procs) {
      proc.await();
    }
  }

  private static String[] toEnvironmentArray(Map<String, String> env) {
    List<String> array = new ArrayList<String>();
    for (Map.Entry<String, String> entry : env.entrySet()) {
      array.add(entry.getKey() + "=" + entry.getValue());
    }
    return array.toArray(new String[env.size()]);
  }

  private static Map<String, String> readEnvironment(String name) throws Exception {
    return readEnvironment(new FileReader("pom.xml"), name);
  }

  @VisibleForTesting
  static Map<String, String> readEnvironment(Reader pom, String name) throws Exception {
    Document document = DocumentHelper.parseText(CharStreams.toString(pom));

    Map<String, String> envConfig = new LinkedHashMap<String, String>();
    List<Node> nodes = select(document, "/m:project/m:profiles/m:profile");
    for (Node node : nodes) {
      List<Node> select = select(node, "m:id");
      if (!select.isEmpty() && name.equals(select.get(0).getStringValue())) {
        List<Node> properties = select(node, "m:properties/*");
        for (Node property : properties) {
          envConfig.put(property.getName(), property.getStringValue());
        }
      }
    }

    return envConfig;
  }

  @SuppressWarnings("unchecked")
  private static List<Node> select(Node document, String expression) {
    DefaultXPath expr = new DefaultXPath(expression);
    expr.setNamespaceURIs(POM_NAMESPACE);

    return expr.selectNodes(document);
  }

  public static List<Proc> readProcs(Config config) throws IOException {
    File procfile = new File("Procfile");
    if (!procfile.exists())
      Cloud.quit("Procfile not found. cannot proceed");


    List<Proc> procs = Lists.newArrayList();
    List<String> lines = CharStreams.readLines(new FileReader(procfile));
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty())
        continue;

      procs.add(new Proc(line, config));
    }
    return procs;
  }
}
