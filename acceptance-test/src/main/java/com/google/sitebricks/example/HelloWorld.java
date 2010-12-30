package com.google.sitebricks.example;

import com.google.sitebricks.At;
import com.google.sitebricks.rendering.EmbedAs;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@At("/hello") @EmbedAs("Hello")
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