package com.google.sitebricks.example;

public class Custom {

  private String encap;
  
  public Custom() {
    encap = "encapsulated valuev";
  }
  
  public Custom(String value) {
    encap = value;
  }
  
  @Override
  public String toString() {
    return encap.toString();
  }
}
