package com.google.sitebricks.cloud;

import com.google.sitebricks.options.Options;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Options
public abstract class Config {
  public String name() {
    return null;
  }

  public String at() {
    return null;
  }

  public String show() {
    return null;
  }

  public boolean force() {
    return false;
  }
}
