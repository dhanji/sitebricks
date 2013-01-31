package com.google.sitebricks.example;

import javax.inject.Inject;

import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.example.dao.SimpleDao;
import com.google.sitebricks.example.model.Person;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.As;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;

@Service
public class RestfulWebServiceValidatingDao {

    @Inject
    private SimpleDao dao;
    
    @Get
    @As(Json.class)
    Reply<Person> newPerson() {
        return Reply.with(new Person());
    }

    @Post
    @As(Json.class)
    Reply<?> postPerson(@As(Json.class) Person person, Request request) {
      dao.save(person);
      return Reply.with(person);
    }

}
