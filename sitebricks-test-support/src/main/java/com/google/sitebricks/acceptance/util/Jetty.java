package com.google.sitebricks.acceptance.util;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

import com.google.inject.Injector;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Jason van Zyl
 */
public class Jetty {
  
  public static final String INJECTOR_KEY = "@_INJECTOR_@";
  private static final String APP_NAME = "/sitebricks";
  private final Server server;

  public Jetty(String path, int port, ClassLoader classLoader, Injector injector) {
    WebAppContext webAppContext = new WebAppContext(path, APP_NAME);
    webAppContext.setClassLoader(classLoader);
    webAppContext.getServletContext().setAttribute(INJECTOR_KEY, injector);
    server = new Server(port);
    server.addHandler(webAppContext);
  }  

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
