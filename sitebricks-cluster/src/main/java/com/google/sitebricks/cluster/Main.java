package com.google.sitebricks.cluster;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.options.OptionsModule;

import java.util.*;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Main {
  public static final Map<String, Class<? extends Command>> commandMap =
      new LinkedHashMap<String, Class<? extends Command>>();
  public static final Map<String, String> descriptions =
      new HashMap<String, String>();

  static {
    commandMap.put("doctor", Doctor.class);
    descriptions.put("doctor", "Finds problems with your setup and suggests how to fix them =)");

    commandMap.put("machines", Machines.class);
    descriptions.put("machines", "Lists all active machines that the overlord knows about");
  }

  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new OptionsModule(args).options(Config.class));
    Config config = injector.getInstance(Config.class);

    // Discover all non-flag switches.
    List<String> commands = new ArrayList<String>();
    for (String arg : args) {
      if (arg.startsWith("-"))
        continue;

      if (arg.contains(":")) {
        String[] split = arg.split(":");
        for (int i = 0; i < split.length; i++) {
          commands.add(split[i]);
        }
      } else
        commands.add(arg);
    }

    if (commands.isEmpty()) {
      System.out.println("Usage: sitebricks <command> <options>\n\n" +
          "Commands:");
      for (String cmd : commandMap.keySet()) {
        System.out.println("  " + cmd + " - " + (descriptions.get(cmd)));
      }

      System.exit(1);
    }

    injector.getInstance(commandMap.get(commands.get(0)))
        .run(commands, config);
  }
}
