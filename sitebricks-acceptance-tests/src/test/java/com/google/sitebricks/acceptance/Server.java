package com.google.sitebricks.acceptance;

import com.google.sitebricks.acceptance.util.JettyAcceptanceTest;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Server {
  public static void main(String[] args) throws Exception {
    JettyAcceptanceTest acceptanceTest = new JettyAcceptanceTest(8080);
    acceptanceTest.start();
    acceptanceTest.join();
  }
}
