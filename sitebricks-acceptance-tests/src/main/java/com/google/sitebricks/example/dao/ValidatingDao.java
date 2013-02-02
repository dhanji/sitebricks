package com.google.sitebricks.example.dao;

import javax.validation.Valid;
import javax.validation.ValidationException;

import org.apache.bval.guice.Validate;

import com.google.sitebricks.example.model.Person;

public class ValidatingDao implements SimpleDao {

    @Validate(
            rethrowExceptionsAs = ValidationException.class,
            exceptionMessage = "Validation exception %s")
    @Override
    public void save(@Valid Person person) {
       // Fake implementation for test only.
    }

}
