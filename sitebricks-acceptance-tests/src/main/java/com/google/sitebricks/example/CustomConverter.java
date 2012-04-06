package com.google.sitebricks.example;

import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;

@At("/customConvertion")
public class CustomConverter {

  public static final String INITIAL_VALUE = "initial value";
  private Custom custom;

  public CustomConverter() {
    custom = new Custom(INITIAL_VALUE);
  }

  @Get
  public void get() {
    System.out.println("custom converter sample.  woot!");
  }

  @Post
  public void post() {
    System.out.println("posted custom value: " + custom);
  }

  public Custom getTestValue() {
    return custom;
  }

  public void setTestValue(Custom value) {
    System.out.println("*************************** " + value);
    custom = value;
  }

}
