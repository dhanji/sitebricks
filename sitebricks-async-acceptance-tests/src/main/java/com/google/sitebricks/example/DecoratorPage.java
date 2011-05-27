package com.google.sitebricks.example;

import com.google.sitebricks.Show;

@Show("/Decorator.html")
public abstract class DecoratorPage {
  public String getHello() {
    return "Hello (from the superclass)";
  }

  public abstract String getWorld();
}
