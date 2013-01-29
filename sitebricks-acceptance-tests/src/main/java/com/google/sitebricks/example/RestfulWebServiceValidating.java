package com.google.sitebricks.example;

import java.util.List;

import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.bval.guice.Validate;

import com.google.common.collect.Lists;
import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.As;
import com.google.sitebricks.http.Post;

@At("/rest/validate")
@Service
public class RestfulWebServiceValidating {

    @Post
    @As(Json.class)
    Reply<List<Person>> getRelatives(@As(Json.class) Person p) {
        return Reply.with(relativesOf(p));
    }

    @Post
    @As(Json.class)
    @Validate(
            rethrowExceptionsAs = ValidationException.class,
            exceptionMessage = "Validation exception %s")
    Reply<List<Person>> postRelatives(@As(Json.class) Person p) {
        return Reply.with(relativesOf(p));
    }

    private List<Person> relativesOf(Person p) {
        return Lists.newArrayList(p);
    }

    public static class Person {
        @NotNull(
                message = "fistName.length.null")
        @Size(
                min = 1, 
                message = "fistName.length.violation"
                )
        private String firstName;
        private String lastName;
        private String surName;
        private int age;
        public String getFirstName() {
            return firstName;
        }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
        public String getLastName() {
            return lastName;
        }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
        public String getSurName() {
            return surName;
        }
        public void setSurName(String surName) {
            this.surName = surName;
        }
        public int getAge() {
            return age;
        }
        public void setAge(int age) {
            this.age = age;
        }

    }

}
