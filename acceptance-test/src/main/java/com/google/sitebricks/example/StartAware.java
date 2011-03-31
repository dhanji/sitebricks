package com.google.sitebricks.example;

import com.google.sitebricks.Aware;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class StartAware implements Aware {
  @Override
  public void startup() {
    HelloWorld.HELLO_MSG = "Hello from google-sitebricks!";
  }

  @Override
  public void shutdown() {
  }
}
