package com.google.sitebricks.example;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class HelloWorld {

  public static final String HELLO_MSG = "Hello from google-sitebricks!";

  private String message = HELLO_MSG;

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}