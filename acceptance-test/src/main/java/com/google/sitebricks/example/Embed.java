package com.google.sitebricks.example;

import com.google.sitebricks.At;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@At("/embed")
public class Embed {

  private List<String> arg = Arrays.asList(
      "Embedding in google-sitebricks is awesome!",
      "Embedding in google-sitebricks totally rules!",
      "google-sitebricks embed FTW!"
  );

  public List<String> getArg() {
    return arg;
  }
}