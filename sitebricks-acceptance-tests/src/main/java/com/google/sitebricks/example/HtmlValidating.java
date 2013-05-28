package com.google.sitebricks.example;

import javax.inject.Inject;

import com.google.sitebricks.acceptance.util.Jetty;
import com.google.sitebricks.example.dao.SimpleDao;
import com.google.sitebricks.example.model.Person;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.i18n.Message;
import com.google.sitebricks.i18n.ResourceBundle;

@ResourceBundle(value = HtmlValidating.ErrorMessages.class)
public class HtmlValidating {
    
    @Inject
    private SimpleDao dao;
    
    private Person person;
    
    public HtmlValidating() {
      this.person = new Person();
    }
    
    @Get
    public void showPage() {
    }

    @Post
    public String createPerson() {
      dao.save(person);
      return Jetty.APP_NAME;
    }

    public Person getPerson() {
      return person;
    }

    public void setPerson(Person person) {
      this.person = person;
    }
    
    public static interface ErrorMessages {
      @Message(message = "Constraint Violation Length First Name Message")
      String constraintViolationLengthFirstName();
      @Message(message = "Constraint Violation Null Age Message")
      String constraintViolationNullAge();
      @Message(message = "Constraint Violation Length Last Name Message")
      String constraintViolationLengthLastName();
    }

}
