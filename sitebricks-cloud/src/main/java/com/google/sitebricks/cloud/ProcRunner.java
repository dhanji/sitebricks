package com.google.sitebricks.cloud;


import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.sitebricks.cloud.proc.DynamicCompilation;
import com.google.sitebricks.cloud.proc.Proc;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class ProcRunner implements Command {
  @Override
  public void run(List<String> commands, Config config) throws Exception {
    List<Proc> procs = readProcs(config);

    // Compile project first, if necessary.
    DynamicCompilation.compile(config);

    // Load environment config.
    String envName = System.getenv("ENV");
    if (envName == null)
      envName = "local";

    Map<String, Object> env = readEnvironment(envName);
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

  private static String[] toEnvironmentArray(Map<String, Object> env) {
    List<String> array = new ArrayList<String>();
    for (Map.Entry<String, Object> entry : env.entrySet()) {
      array.add(entry.getKey() + "=" + entry.getValue());
    }
    return array.toArray(new String[env.size()]);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> readEnvironment(String name) throws FileNotFoundException {
    Yaml yaml = new Yaml();

    Map<String, Object> envConfig = (Map<String, Object>) yaml.load(new FileReader("config/environment.yml"));
    return  (Map<String, Object>) envConfig.get(name);
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
