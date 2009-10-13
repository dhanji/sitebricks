package com.google.sitebricks.example;

import com.google.sitebricks.At;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@At("/embed")
public class Embed {

  private String arg = "Embedding in google-sitebricks is awesome!";

  public String getArg() {
    return arg;
  }

  public void setArg(String arg) {
    this.arg = arg;
  }
}