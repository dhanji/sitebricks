package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.acceptance.util.Jetty;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * @author Tom Wilson (tom@tomwilson.name)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class JettyAcceptanceTest {
    private Jetty server;

    public JettyAcceptanceTest() {
        server = new Jetty();
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
