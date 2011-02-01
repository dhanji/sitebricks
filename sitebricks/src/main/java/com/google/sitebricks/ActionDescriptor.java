package com.google.sitebricks;

import com.google.sitebricks.routing.Action;

import java.lang.annotation.Annotation;
import java.util.regex.Pattern;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class ActionDescriptor implements PageBinder.ActionBinder {
  private final Action action;

  public ActionDescriptor(Action action) {
    this.action = action;
  }

  @Override
  public PageBinder.ActionBinder on(Class<? extends Annotation>... method) {
    return this;
  }

  @Override
  public PageBinder.ActionBinder select(String param, String value) {
    return this;
  }

  @Override
  public PageBinder.ActionBinder selectHeader(String param, String value) {
    return this;
  }

  @Override
  public PageBinder.ActionBinder selectHeader(String param, Pattern regex) {
    return this;
  }
}
