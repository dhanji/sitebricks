package com.google.sitebricks.cluster;

import com.google.sitebricks.cloud.Config;
import com.google.sitebricks.cloud.ProcRunner;
import com.google.sitebricks.cloud.proc.Proc;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Cluster sentinel entry point. This is the guy that manages all the running procs,
 * monitoring them etc.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class NodeManager extends AbstractHandler {
  private static final Logger log = LoggerFactory.getLogger("sitebricks");

  private static final ExecutorService executor = Executors.newCachedThreadPool();
  private static volatile String[] environment = new String[] {};
  public static final int MAX_ATTEMPTS = 3;

  private final List<Proc> procs;

  public NodeManager(List<Proc> procs) {
    this.procs = procs;
  }

  public static void main(String[] args) throws Exception {
    List<Proc> procs = ProcRunner.readProcs(new Config() {});

    Server server = new Server(12012);
    server.setHandler(new NodeManager(procs));
    server.start();

    log.info("sitebricks cluster manager started");
    // Start them.
    for (final Proc proc : procs) {
      tryStart(proc, 1);
    }

    server.join();
  }

  @Override
  public void handle(String target, Request baseRequest, HttpServletRequest request,
                     HttpServletResponse response) throws IOException, ServletException {
    response.setStatus(200);
    baseRequest.setHandled(true);

    if ("/stop/all".equals(target)) {
      boolean ok = true;
      for (Proc proc : procs) {
        ok = ok & tryStop(proc, 1);
      }

      if (!ok) {
        sendError(response, "Error stopping one or more jobs (check logs)");
        return;
      }
    }

    sendOk(response);
  }

  private void sendOk(HttpServletResponse response) throws IOException {
    PrintWriter writer = response.getWriter();
    writer.write("OK\n");
    writer.close();
  }

  private void sendError(HttpServletResponse response, String error) throws IOException {
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error);
    response.getWriter().close();
  }

  private static boolean tryStart(Proc proc, int attempt) {
    try {
      if (attempt > 1)
        Thread.sleep(TimeUnit.SECONDS.toMillis(2L));

      proc.start(environment);
      executor.submit(new ProcStarter(proc));

      return true;
    } catch (Exception e) {
      if (attempt < MAX_ATTEMPTS) {
        return proc.running() || tryStart(proc, attempt + 1);
      } else {
        log.info("giving up after {} failed starts of '{}'", MAX_ATTEMPTS, proc.name());
        return false;
      }
    }
  }

  private static boolean tryStop(Proc proc, int attempt) {
    try {
      if (attempt > 1)
        Thread.sleep(TimeUnit.SECONDS.toMillis(2L));

      return proc.stop();
    } catch (Exception e) {
      if (attempt < MAX_ATTEMPTS) {
        return tryStop(proc, attempt + 1);
      } else {
        log.info("giving up, unable to stop '{}' after {} attempts", proc.name(), MAX_ATTEMPTS);
        return false;
      }
    }
  }

  private static class ProcStarter implements Runnable {
    private final Proc proc;

    private ProcStarter(Proc proc) {
      this.proc = proc;
    }

    @Override
    public void run() {
      try {
        proc.await();
      } catch (Exception e) {
        // start failed.
        log.debug("detected job death {}", proc.name());
      }

      if (proc.isOn() && !proc.running()) {
        tryStart(proc, 0);
      }
    }
  }
}
