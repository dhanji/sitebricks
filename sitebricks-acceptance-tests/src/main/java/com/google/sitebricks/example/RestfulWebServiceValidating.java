package com.google.sitebricks.example;

import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.conversion.ValidationConverter;
import com.google.sitebricks.example.model.Person;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.As;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;

@Service
public class RestfulWebServiceValidating {
    
    @Inject
    private ValidationConverter validationConverter;

    @Get
    @As(Json.class)
    Reply<Person> newPerson() {
      return Reply.with(new Person());
    }

    @Post
    @As(Json.class)
    Reply<?> postPerson(@As(Json.class) Person person, Request request) {
      try {
          request.validate(person);
      }
      catch (ValidationException ve) {
          ConstraintViolationException cve = (ConstraintViolationException) ve.getCause();
          Set<? extends ConstraintViolation<?>> scv = (Set<? extends ConstraintViolation<?>>) cve.getConstraintViolations();
          return Reply.with(validationConverter.to(scv)).badRequest();
      }
      return Reply.with(person);
    }

}
