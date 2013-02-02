package com.google.sitebricks.acceptance.util;


import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import com.google.inject.Injector;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Jason van Zyl
 */
public class Jetty {

  public static final String INJECTOR_KEY = "@_INJECTOR_@";
  public static final String APP_NAME = "/sitebricks";
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
    // Hack to allow successfull test via maven CLI
    // Read http://stackoverflow.com/questions/2151075/cannot-load-jstl-taglib-within-embedded-jetty-server
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    File taglibsJarFile = new File(System.getProperty("user.home") + "/.m2/repository/taglibs/standard/1.1.2/standard-1.1.2.jar");
    if (! taglibsJarFile.exists()) {
        throw new RuntimeException("Taglib Jar file does not exist at path: " + taglibsJarFile.getAbsolutePath());
    }
    URL taglibsJarUrl = taglibsJarFile.toURI().toURL();
    URLClassLoader newClassLoader = new URLClassLoader(new URL[]{taglibsJarUrl}, currentClassLoader);
    Thread.currentThread().setContextClassLoader(newClassLoader);
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
