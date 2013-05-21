package com.google.sitebricks.ext;

import org.apache.bval.guice.ValidationModule;

import com.google.inject.AbstractModule;

/**
 * Module encapsulates external bindings for sitebricks validation. Can be installed multiple times.
 */
public class SitebricksValidationExtModule extends AbstractModule {

  @Override
  protected void configure() {
      install(new ValidationModule());
  }

  @Override
  public int hashCode() {
    return SitebricksValidationExtModule.class.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return SitebricksValidationExtModule.class.isInstance(obj);
  }
}
