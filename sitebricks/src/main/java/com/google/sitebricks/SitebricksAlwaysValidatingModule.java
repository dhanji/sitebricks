package com.google.sitebricks;

import javax.validation.Validator;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.sitebricks.validation.AlwaysValidationValidator;

/**
 * Module encapsulates external bindings for sitebricks validation.
 * It will always validate.
 * Can be installed multiple times.
 */
public class SitebricksAlwaysValidatingModule extends AbstractModule {

  @Override
  protected void configure() {
      bind(Validator.class).to(AlwaysValidationValidator.class).in(Scopes.SINGLETON);
  }

  @Override
  public int hashCode() {
    return SitebricksAlwaysValidatingModule.class.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return SitebricksAlwaysValidatingModule.class.isInstance(obj);
  }
}
