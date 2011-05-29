package com.google.sitebricks.async;

import com.google.inject.Guice;
import com.google.sitebricks.Sitebricks;
import com.google.sitebricks.SitebricksAsyncModule;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SitebricksAsyncIntegrationTest {
  public static void main(String[] args) {
    Guice.createInjector(new SitebricksAsyncModule())
        .getInstance(Sitebricks.class)
        .start();
  }
}
