package com.google.sitebricks.conversion;

import java.util.List;
import java.util.Set;

import javax.inject.Singleton;
import javax.validation.ConstraintViolation;

import com.google.common.collect.Lists;

@Singleton
public class ValidationConverter extends ConverterAdaptor<Set<? extends ConstraintViolation<?>>, List<String>> {

    @Override
    public List<String> to(Set<? extends ConstraintViolation<?>> source) {
        List<String> errors = Lists.newArrayList();
        if (source != null) {
            for (ConstraintViolation<?> cv: source) {
                errors.add(cv.getMessage());
            }
        }
        return errors;
    }
    
}
