package com.google.sitebricks.ext;

import org.apache.bval.guice.ValidationModule;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.sitebricks.validation.SitebricksBValValidator;
import com.google.sitebricks.validation.SitebricksValidator;

/**
 * Module encapsulates external bindings for sitebricks validation
 * with BVal Guice.
 */
public class SitebricksValidationExtModule extends AbstractModule {

  @Override
  protected void configure() {
      install(new ValidationModule());
      bind(SitebricksValidator.class).to(SitebricksBValValidator.class).in(Scopes.SINGLETON);
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
