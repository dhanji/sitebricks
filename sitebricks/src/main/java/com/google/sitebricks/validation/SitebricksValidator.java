package com.google.sitebricks.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;

import com.google.inject.ImplementedBy;

@ImplementedBy(SitebricksValidator.class)
public interface SitebricksValidator {
    
    Set<? extends ConstraintViolation<?>> validate(Object object);

}
