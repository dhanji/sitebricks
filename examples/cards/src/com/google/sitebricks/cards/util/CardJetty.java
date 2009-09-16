package com.google.sitebricks.cards.util;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class CardJetty {
  private final Server server;

  public CardJetty() {
    this(new WebAppContext("web", "/cards"), 8080);
  }

  public CardJetty(WebAppContext webAppContext, int port) {
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

  public static void main(String... args) throws Exception {
    CardJetty jetty = new CardJetty();
    jetty.start();
    jetty.join();
  }
}
