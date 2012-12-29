package com.google.sitebricks.cloud;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.options.OptionsModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Cloud {
  private static final Logger log = LoggerFactory.getLogger("sitebricks");

  public static final Map<String, Class<? extends Command>> commandMap =
      new LinkedHashMap<String, Class<? extends Command>>();
  public static final Map<String, String> descriptions =
      new HashMap<String, String>();

  static {
    commandMap.put("doctor", Doctor.class);
    descriptions.put("doctor", "Finds problems with your setup and suggests how to fix them =)");

    commandMap.put("machines", Machines.class);
    descriptions.put("machines", "Lists all active machines that the overlord knows about");

    commandMap.put("run", ProcRunner.class);
    descriptions.put("run", "Runs the cluster locally");
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
        Collections.addAll(commands, split);
      } else
        commands.add(arg);
    }

    if (commands.isEmpty())
      commands = Lists.newArrayList("run");

    if (commands.isEmpty()) {
      System.out.println("Usage: sitebricks <command> <options>\n\n" +
          "Commands:");
      for (String cmd : commandMap.keySet()) {
        System.out.println("  " + cmd + " - " + (descriptions.get(cmd)));
      }

      System.exit(1);
    }

    try {
      injector.getInstance(commandMap.get(commands.get(0)))
          .run(commands, config);
      System.exit(0);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static void quit(String message) {
    log.info(message);
    System.exit(1);
  }
}
