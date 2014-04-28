package com.google.sitebricks.example;

import com.google.inject.TypeLiteral;
import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.http.Put;

import java.util.List;

/**
 * @author Miroslav Genov (mgenov@gmail.com)
 */
@At("/serviceWithGenerics") @Service
public class RestfulWebServiceWithGenerics {

  @Post
  public Reply<?> addPerson(Request request) {

    List<Person> personList = request.read(new TypeLiteral<List<Person>>() {}).as(Json.class);

    return Reply.with(personList).as(Json.class);
  }

  public static class Person {
    private String name;

    Person() {

    }

    public Person(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
}
