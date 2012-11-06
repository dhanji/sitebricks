package com.google.sitebricks.cloud.proc;

import com.google.sitebricks.cloud.Cloud;
import com.google.sitebricks.cloud.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Proc {
  private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(
      Runtime.getRuntime().availableProcessors());

  private final Logger log;

  private final String name;
  private final String command;
  private final Config config;
  private volatile boolean on;

  private final Runnable logTask = new Runnable() {
    @Override
    public void run() {
      if (process == null)
        return;

      try {
        pipe();
      } catch (IOException e) {
        log.error("Process communication error", e);
      }

      if (process != null)
        executor.schedule(logTask, 1, TimeUnit.SECONDS);
    }
  };

  private volatile Process process;
  private volatile BufferedInputStream out;
  private volatile BufferedInputStream err;
  private volatile String pid;

  public Proc(String line, Config config) {
    this.config = config;

    String[] pieces = line.split("\\s*:\\s*", 2);
    if (pieces.length < 2)
      Cloud.quit("process definition is malformed:\n --> " + line);

    name = pieces[0].trim();
    command = pieces[1].trim();

    log = LoggerFactory.getLogger(name);
  }

  public boolean running() {
    return process != null;
  }

  public boolean isOn() {
    return on;
  }

  public String name() {
    return name;
  }

  public String command() {
    return command;
  }

  public void start(String[] environment) throws Exception {
    on = true;
    try {
      this.process = Runtime.getRuntime().exec(command, environment);
    } catch (Exception e) {
      log.info(e.getMessage());
      throw e;
    }
    this.out = new BufferedInputStream(process.getInputStream());
    this.err = new BufferedInputStream(process.getErrorStream());

    // Assume running on unix, try to discover the pid.
    try {
      Field field = process.getClass().getDeclaredField("pid");
      field.setAccessible(true);
      pid = field.get(process).toString();
    } catch (Exception e) {
      pid = "[unsupported on this platform]";
    }

    executor.schedule(logTask, 1, TimeUnit.SECONDS);
    log.info("started with pid {}", pid);
  }

  public boolean stop() throws IOException {
    on = false;
    if (!running())
      return true;

    boolean areWeSureItStopped = true;
    process.destroy();

    // Unix only. See if the process didn't die after all.
    if (pid != null) {
      boolean stillUp = false;
      try {
        Process exec = Runtime.getRuntime().exec("ps ux");
        BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        while (reader.ready())
          if (reader.readLine().contains(" " + pid + " ")) {
            stillUp = true;
            break;
          }

        exec.destroy();
      } catch (IOException e) {
        stillUp = true; /* attempt to kill it anyway */
        log.info(e.getMessage());
      }

      if (stillUp) {
        try {
          Runtime.getRuntime().exec("kill -9 " + pid);
        } catch (IOException e) {
          areWeSureItStopped = false;
          log.info(e.getMessage());
        }
      } else
        cleanup();
    }

    return areWeSureItStopped;
  }

  public int await() throws Exception {
    try {
      return process.waitFor();
    } finally {
      if (isOn())
        cleanup();
    }
  }

  private void cleanup() throws IOException {
    pipe();
    log.info("terminated");
    process = null;
    out = null;
    err = null;
    pid = null;
  }

  private void pipe() throws IOException {
    pipe(process.getInputStream());
    pipe(process.getErrorStream());
  }

  private void pipe(InputStream in) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    while (reader.ready()) {
      log.info(reader.readLine());
    }
  }
}
