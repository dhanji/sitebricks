package com.google.sitebricks.acceptance.util;

import java.io.File;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * @author Tom Wilson (tom@tomwilson.name)
 */
public class JettyAcceptanceTest { 
  private static final String BUILDR_RESOURCE_DIR = "acceptance-test/src/main/resources";
  private static final String STD_RESOURCE_DIR = "src/main/resources";

  private Jetty server;

  public JettyAcceptanceTest() {
      File standardDir = new File(STD_RESOURCE_DIR);

      if (standardDir.exists()) {
        server = new Jetty(STD_RESOURCE_DIR);
      } else {
        server = new Jetty(BUILDR_RESOURCE_DIR);
      }
    }

    @BeforeSuite
    public void start() throws Exception {
        server.start();
    }

    @AfterSuite
    public void stop() throws Exception {
        server.stop();
    }
}
