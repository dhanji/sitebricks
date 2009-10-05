package com.google.sitebricks.tutorial;

import com.google.sitebricks.At;

@At("/")
public class Example {
    private String message = "Hello";

    public String getMessage() { return message; }
}