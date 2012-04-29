package com.google.sitebricks.acceptance.util;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class Jetty {
  private static final String APP_NAME = "/sitebricks";

  private final Server server;

  public Jetty(String path, int port) {
    this(new WebAppContext(path, APP_NAME), port);
  }  
  
  public Jetty(WebAppContext webAppContext, int port) {
    server = new Server(port);
    server.addHandler(webAppContext);
  }

  public void start() throws Exception {
    server.start();
  }

  public void join() throws Exception {
    server.join();
  }

  public void stop() throws Exception {
    server.stop();
  }
}
