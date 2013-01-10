package com.google.sitebricks.cloud;

import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class EnvConfigurer implements Command {
  @Override
  public void run(List<String> commands, Config config) throws Exception {
    if (commands.size() == 1 && "config".equals(commands.get(0))) {
      // List all env variables.
      Map<String,String> env = ProcRunner.readEnvironment(config.env());
      for (Map.Entry<String, String> entry : env.entrySet()) {
        System.out.println(entry.getKey() + ": " + entry.getValue());
      }
    } else {
      String[] command = commands.get(0).split(":");
      if (command.length < 2)
        return;

      if ("get".equals(command[1])) {
        if (commands.size() < 2)
          Cloud.quit("Usage: sitebricks config:get <NAME1> <NAME2> ...");

        Map<String,String> env = ProcRunner.readEnvironment(config.env());
        for (int i = 1; i < commands.size(); i++) {
          String var = commands.get(i);

          String value = env.get(var);
          System.out.println(var + ": " + (value == null ? "<not set>" : value));
        }
      } else if ("set".equals(command[1])) {
        if (commands.size() < 2)
          Cloud.quit("Usage: sitebricks config:set <NAME1>=<VALUE1> <NAME2>=<VALUE2> ...");

        throw new AssertionError("TBI");
      }
    }

    System.out.println();
  }
}
