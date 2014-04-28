package org.sitebricks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.google.sitebricks.options.OptionsModule;
import org.sitebricks.web.NettyServer;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class App {
  public static final String LOGGER = "sitebricks";
  private static final String YAML_FORMAT_EXAMPLE =
      "\nlocal:" +
      "\n  port: 8000" +
      "\n  my_var: <value>" +
      "\nstaging:" +
      "\n  port: 9000" +
      "\n  my_var: <value>\n";

  private final List<Module> modules = new ArrayList<>();
  private final List<String> errors = new ArrayList<>();
  private final Collection<Map<String, String>> freeOptions = Lists.newArrayList();

  private int port = 8080;

  public App(String[] args) {
    configureLogging();

    String env = System.getenv().get("SB_ENV");
    if (env == null)
      env = "local";

    Yaml yaml = new Yaml();

    String fileName = "config.yml";

    try (Reader reader = new File(fileName).exists()
        ? new FileReader(fileName)
        : new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName))) {
      @SuppressWarnings("unchecked")
      Map<String, Object> yamlFile = (Map<String, Object>) yaml.load(reader);
      @SuppressWarnings("unchecked")
      Map<String, Object> options = (Map)yamlFile.get(env);

      // TODO Load entire Yaml tree and match to config class's object graph.
      Object port = options.get("port");
      if (port != null)
        this.port = Integer.valueOf(port.toString());

      // HACK.
      Map<String, String> cleaned = new HashMap<>();
      for (Map.Entry<String, Object> entry : options.entrySet()) {
        cleaned.put(entry.getKey(), entry.getValue().toString());
      }

      freeOptions.add(cleaned);
    } catch (IOException | NullPointerException e) {
      // Ignore.
      LoggerFactory.getLogger(LOGGER)
          .warn("Unable to find {}. Reading config from command line only.", fileName);
    } catch (Exception e) {
      // Ignore.
      errors.add(String.format("%s is malformed. Must follow the following format: %s", fileName, YAML_FORMAT_EXAMPLE));
    }
  }

  public App configure(Class<?> configClass, String[] args) {
    modules.add(new OptionsModule(args, freeOptions).options(configClass));
    return this;
  }

  public App modules(Module... modules) {
    this.modules.addAll(Arrays.asList(modules));
    return this;
  }

  public void start() {
    Injector injector = Guice.createInjector(this.modules);

    injector.getInstance(NettyServer.class).start(port);
  }

  private void configureLogging() {
    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
    lc.reset();
    PatternLayoutEncoder ple = new PatternLayoutEncoder();
    ple.setPattern("%level %logger{10} - %msg%n");
    ple.setContext(lc);
    ple.start();

    ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
    appender.setContext(lc);
    appender.setEncoder(ple);
    appender.setName("STDOUT");
    appender.start();

    // Silence jetty a bit
    Logger root = lc.getLogger("root");
    root.addAppender(appender);
    root.setLevel(Level.INFO);
//    lc.getLogger("org.eclipse.jetty").setLevel(Level.INFO);
  }
}
