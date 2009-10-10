package com.google.sitebricks.acceptance.util;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class Jetty {
    private final Server server;

    public Jetty() {
        this(new WebAppContext("resources", "/sitebricks"), 4040);
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

    public static void main(String... args) throws Exception {
        Jetty jetty = new Jetty();
        jetty.start();
        jetty.join();
    }
}
