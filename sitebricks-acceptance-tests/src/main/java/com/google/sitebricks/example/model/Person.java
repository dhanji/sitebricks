package com.google.sitebricks.example.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Person {
    @NotNull(message = "constraintViolationNullFirstName")
    @Size(min = 1, message = "constraintViolationLengthFirstName")
    private String firstName;
    @NotNull(message = "constraintViolationNullLastName")
    @Size(min = 1, message = "constraintViolationLengthLastName")
    private String lastName;
    private String surName;
    @NotNull(message = "constraintViolationNullAge")
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
