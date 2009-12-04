package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.acceptance.util.Jetty;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;

/**
 * @author Tom Wilson (tom@tomwilson.name)
 */
@Test(suiteName = AcceptanceTest.SUITE)
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
