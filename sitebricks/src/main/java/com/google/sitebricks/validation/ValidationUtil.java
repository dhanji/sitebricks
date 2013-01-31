package com.google.sitebricks.validation;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import com.google.common.collect.Lists;

public class ValidationUtil {
    
    public static List<String> toErrors(ValidationException ve) {
        ConstraintViolationException cve = (ConstraintViolationException) ve.getCause();
        return toErrors((Set<? extends ConstraintViolation<?>>) cve.getConstraintViolations());
    }
    
    public static List<String> toErrors(Set<? extends ConstraintViolation<?>> cvs) {
        List<String> errors = Lists.newArrayList();
        for (ConstraintViolation<?> cv: cvs) {
            errors.add(cv.getMessage());
        }
        return errors;
    }
    
}
