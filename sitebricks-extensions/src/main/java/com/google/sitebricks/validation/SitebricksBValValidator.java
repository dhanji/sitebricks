package com.google.sitebricks.validation;

import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

public class SitebricksBValValidator implements SitebricksValidator {
    
  @Inject
  private Validator validator;

  @Override
  public Set<? extends ConstraintViolation<?>> validate(Object object) {
    return this.validator.validate(object);
  }

}
