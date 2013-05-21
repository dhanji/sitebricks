package com.google.sitebricks.validation;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.metadata.BeanDescriptor;

public class AlwaysValidationValidator implements SitebricksValidator {

    @Override
    public Set<? extends ConstraintViolation<?>> validate(Object object) {
        return null;
    }

}
