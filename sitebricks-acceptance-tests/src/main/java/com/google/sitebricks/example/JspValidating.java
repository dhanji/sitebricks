package com.google.sitebricks.example;

import javax.inject.Inject;

import com.google.sitebricks.Show;
import com.google.sitebricks.acceptance.util.Jetty;
import com.google.sitebricks.example.dao.SimpleDao;
import com.google.sitebricks.example.model.Person;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;

/**
 * 
 */
@Show("/JspValidating.jsp")
public class JspValidating {
    
    @Inject
    private SimpleDao dao;
    
    private Person person;
    
    public JspValidating() {
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
    
}
