package com.google.sitebricks.example;

import javax.inject.Inject;

import com.google.sitebricks.Show;
import com.google.sitebricks.acceptance.util.Jetty;
import com.google.sitebricks.example.dao.SimpleDao;
import com.google.sitebricks.example.model.Person;
import com.google.sitebricks.http.As;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.i18n.Message;
import com.google.sitebricks.i18n.ResourceBundle;
import com.google.sitebricks.transport.Form;

@Show("/JspValidatingAsForm.jsp")
@ResourceBundle(value = JspValidatingAsForm.ErrorMessages.class)
public class JspValidatingAsForm {
    
    @Inject
    private SimpleDao dao;
    
    public JspValidatingAsForm() {
    }
    
    @Get
    public void showPage() {
    }

    @Post
    public String createPerson(@As(Form.class) Person person) {
      return Jetty.APP_NAME;
    }

    public static interface ErrorMessages {
      @Message(message = "Constraint Violation Length First Name Message")
      String constraintViolationLengthFirstName();
      @Message(message = "Constraint Violation Null First Name Message")
      String constraintViolationNullFirstName();
      @Message(message = "Constraint Violation Length Last Name Message")
      String constraintViolationLengthLastName();
      @Message(message = "Constraint Violation Null Last Name Message")
      String constraintViolationNullLastName();
      @Message(message = "Constraint Violation Null Age Message")
      String constraintViolationNullAge();
    }

}
