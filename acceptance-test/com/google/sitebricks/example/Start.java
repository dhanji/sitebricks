package com.google.sitebricks.example;

import com.google.sitebricks.At;
import com.google.sitebricks.Show;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@At("/") @Show("index.html")
public class Start {
    private String message = "Hello from warp-sitebricks!";

    public String getMessage() {
        return message;
    }
}
