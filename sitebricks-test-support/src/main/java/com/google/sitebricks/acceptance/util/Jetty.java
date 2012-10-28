package com.google.sitebricks.acceptance.util;


import com.google.inject.Injector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Jason van Zyl
 */
public class Jetty {

  public static final String INJECTOR_KEY = "@_INJECTOR_@";
  private static final String APP_NAME = "/sitebricks";
  private static int PORT;
  private Server server;

  public Jetty(String path, int port, ClassLoader classLoader, Injector injector) {
    WebAppContext webAppContext = new WebAppContext(path, APP_NAME);
    webAppContext.setClassLoader(classLoader);
    webAppContext.getServletContext().setAttribute(INJECTOR_KEY, injector);
    setUp(webAppContext, port);
  }

  public Jetty(String path, int port) {
    setUp(new WebAppContext(path, APP_NAME), port);
  }

  public Jetty(WebAppContext webAppContext, int port) {
    setUp(webAppContext, port);
  }

  public Jetty(String path) {
    WebAppContext webAppContext = new WebAppContext(path, APP_NAME);
    setUp(webAppContext, 0);
  }

  private void setUp(WebAppContext webAppContext, int port) {
    server = new Server(port);
    server.setHandler(webAppContext);
  }

  public void start() throws Exception {
    server.start();
    //
    // When the server starts if the port is specified at 0, then it will find a free port. Once that
    // happens we'll store it so that client code can rely on a valid and free port number.
    //
    PORT = getListeningPort();
  }

  public void join() throws Exception {
    server.join();
  }

  public void stop() throws Exception {
    server.stop();
  }

  public int getListeningPort() {
    for (Connector connector : server.getConnectors()) {
      return connector.getLocalPort();
    }
    throw new IllegalStateException("No port bound");
  }

  public static String baseUrl() {
    return "http://localhost:" + PORT + "/sitebricks";
  }

}
