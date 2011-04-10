package com.google.sitebricks.mail.webapp;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Jetty {
  private static final String APP_NAME = "/";
  private static final int PORT = 8088;

  private final Server server;

  public Jetty() {
    this(new WebAppContext("src/test/resources", APP_NAME), PORT);
  }

  public Jetty(WebAppContext webAppContext, int port) {
    server = new Server(port);
    server.addHandler(webAppContext);
  }

  public static void main(String... args) throws Exception {
    Jetty jetty = new Jetty();
    jetty.server.start();
    jetty.server.join();
  }
}
