package com.google.sitebricks.example;

import com.google.sitebricks.http.Get;

public class VelocitySample {

    public static final String MSG = "Yaba Daba Doo!";

    @Get
    public void get() {
        System.out.println("velocity sample.  woot!");
    }

    public String getName() {
        return "fred flinstone";
    }

    public String getMessage() {
        return MSG;
    }
}
