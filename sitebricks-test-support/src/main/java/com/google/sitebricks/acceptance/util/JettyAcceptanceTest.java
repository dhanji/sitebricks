package com.google.sitebricks.acceptance.util;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.File;

/**
 * @author Tom Wilson (tom@tomwilson.name)
 */
public class JettyAcceptanceTest { 
  private static final String BUILDR_RESOURCE_DIR = "sitebricks-acceptance-tests/src/main/resources";
  private static final String STD_RESOURCE_DIR = "src/main/resources";

  private Jetty server;

  public JettyAcceptanceTest() {
    this(0);
  }

  public JettyAcceptanceTest(int port) {
      File standardDir = new File(STD_RESOURCE_DIR);

      if (standardDir.exists()) {
        server = new Jetty(STD_RESOURCE_DIR, port);
      } else {
        server = new Jetty(BUILDR_RESOURCE_DIR, port);
      }
    }

    @BeforeSuite
    public void start() throws Exception {
        server.start();
    }

    public void join() throws Exception {
        server.join();
    }

    @AfterSuite
    public void stop() throws Exception {
        server.stop();
    }
}
