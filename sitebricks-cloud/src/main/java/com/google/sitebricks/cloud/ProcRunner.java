package com.google.sitebricks.cloud;


import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.sitebricks.cloud.proc.Proc;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class ProcRunner implements Command {
  @Override
  public void run(List<String> commands, Config config) throws Exception {
    List<Proc> procs = readProcs(config);

    // Start procs.
    String[] environment = new String[] { "env=local" };
    for (Proc proc : procs) {
      proc.start(environment);
    }
    LoggerFactory.getLogger("sitebricks").info("all jobs started");

    for (Proc proc : procs) {
      proc.await();
    }
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
