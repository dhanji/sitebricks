package com.google.sitebricks.cloud;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.cloud.mix.Mixes;
import com.google.sitebricks.options.OptionsModule;
import org.mvel2.templates.TemplateRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Cloud {
  private static final Set<String> MAVEN_REPOS = new LinkedHashSet<String>();
  public static final String SB_VERSION = System.getenv("SB_VERSION");

  static {
    if (SB_VERSION == null) {
      System.out.println("Missing environment variable 'SB_VERSION'");
      System.exit(1);
    }

    // Add deps to classpath. Warning this is fragile as must be maintained
    // between pom and here in sync.
    try {
      addDepToClasspath("org.slf4j:slf4j-api:1.6.4");
      addDepToClasspath("ch.qos.logback:logback-classic:0.9.26");
      addDepToClasspath("ch.qos.logback:logback-core:0.9.26");
      addDepToClasspath("javax.inject:javax.inject:1");
      addDepToClasspath("aopalliance:aopalliance:1.0");
      addDepToClasspath("com.google.inject:guice:3.0");
      addDepToClasspath("com.google.sitebricks:sitebricks-options:" + SB_VERSION);
      addDepToClasspath("com.google.sitebricks:sitebricks-converter:" + SB_VERSION);
      addDepToClasspath("cglib:cglib-full:2.0.2");
      addDepToClasspath("org.yaml:snakeyaml:1.10");
      addDepToClasspath("com.google.guava:guava:r09");
      addDepToClasspath("org.mvel:mvel2:2.1.3.Final");
      addDepToClasspath("dom4j:dom4j:1.6.1");
      addDepToClasspath("jaxen:jaxen:1.1.4");

      // Only assign logger after deps have loaded.
      log = LoggerFactory.getLogger("sitebricks");
    } catch (Exception e) {
      System.out.println("Corrupt dependencies. Try: sitebricks selfupdate");
      System.exit(1);
    }
  }

  private static volatile Logger log;

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

    commandMap.put("init", Init.class);
    descriptions.put("init", "Creates a new sitebricks project");

    commandMap.put("mix", Mixin.class);
    descriptions.put("mix", "Mixes in a sitebricks component");

    commandMap.put("mixes", Mixes.class);
    descriptions.put("mixes", "Lists all available components");

    commandMap.put("config", EnvConfigurer.class);
    commandMap.put("config:get", EnvConfigurer.class);
    commandMap.put("config:set", EnvConfigurer.class);
    descriptions.put("config", "List or edit all configured environment variables");
  }

  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new OptionsModule(args).options(Config.class));
    Config config = injector.getInstance(Config.class);

    // Discover all non-flag switches.
    List<String> commands = new ArrayList<String>();
    for (String arg : args) {
      if (arg.startsWith("-"))
        continue;

      commands.add(arg);
    }

    if (commands.isEmpty())
      commands = Lists.newArrayList("run");

    if ("help".equals(commands.get(0))) {
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
    System.out.println(message);
    System.exit(1);
  }

  public static void addDepToClasspath(String dep) throws Exception {
    String jarPath = toJarPath(dep);

    // Rebuild local repository URL.
    String file = System.getProperty("user.home") + "/.m2/repository/" + jarPath;

    File jarFile = new File(file);
    if (!jarFile.exists()) {
      // Don't use #quit() as it invokes logback which may not be available...
      System.out.println("Fatal error, missing internal dependency: " + dep);
      System.out.print("Attempt to fetch from central (y/N)? ");
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      String value = reader.readLine();
      System.out.println();
      if ("yes".equalsIgnoreCase(value) || "y".equalsIgnoreCase(value)) {
        fetchDependency(dep);
        addDepToClasspath(dep);
        return;
      }

      System.exit(1);
    }

    addJarPathToClasspath(jarFile);
  }

  private static void addJarPathToClasspath(File jarFile)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
      MalformedURLException {
    Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
    method.setAccessible(true);
    method.invoke(ClassLoader.getSystemClassLoader(), jarFile.toURI().toURL());
  }

  private static String toJarPath(String dep) {
    return toPath(dep, "jar");
  }

  private static String toPath(String dep, String ext) {
    String[] split = dep.split(":");
    String jar = split[1] + "-" + split[2] + "." + ext;

    return split[0].replace('.', '/')
        + '/'
        + split[1]
        + '/'
        + split[2]
        + '/' + jar;
  }

  private static String toRepoString() {
    return MAVEN_REPOS.toString().replaceAll("[\\[\\] ]", "");
  }

  // Use maven dependency:get to fetch dep from central.
  private static void fetchDependency(String dep) throws Exception {
    String repo = toRepoString();

    System.out.print("Resolving ...");
    String command = "mvn dependency:get -Dartifact=" + dep +
        " -DrepoUrl=" + repo +
        " --batch-mode";
    Process process = Runtime.getRuntime().exec(command);

    if (process.waitFor() == 0)
      System.out.println(" success!");
    else {
      System.out.println("failed");
      System.out.println(command);
      System.out.println();
      BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
      while (br.ready())
        System.out.println(br.readLine());
      System.exit(1);
    }
  }

  public static void mkdir(String path) {
    File dir = new File(path);
    if (dir.exists()) {
      log.warn(path + " already exists. skipped.");
      return;
    }

    log.info(path + "/");
    if (!dir.mkdirs())
      quit("IO error. Unable to mkdir " + path);
  }

  public static void writeFile(String to, String text) throws IOException {
    log.info("writing {}", to);
    final FileWriter fileWriter = new FileWriter(to);
    fileWriter.write(text);
    fileWriter.flush();
    fileWriter.close();
  }

  public static void writeTemplate(String name, Map<String, Object> properties) throws IOException {
    writeTemplate(name, name, properties);
  }

  public static void writeTemplate(String template, String to, Map<String, Object> properties)
      throws IOException {
    writeFile(to, TemplateRuntime.eval(
        Resources.toString(Init.class.getResource(template + ".mvel"), Charsets.UTF_8), properties)
        .toString());
  }
}
