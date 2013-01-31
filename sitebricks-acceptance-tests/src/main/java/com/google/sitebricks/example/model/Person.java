package com.google.sitebricks.example.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Person {
    @NotNull(message = "violation.null.firstName")
    @Size(min = 1, message = "violation.length.firstName")
    private String firstName;
    @NotNull(message = "violation.null.lastName")
    @Size(min = 1, message = "violation.length.lastName")
    private String lastName;
    private String surName;
    @NotNull(message = "violation.null.age")
    private Integer age;
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
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }

}
