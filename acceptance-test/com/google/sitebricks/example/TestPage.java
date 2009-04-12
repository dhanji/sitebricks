package com.google.sitebricks.example;

import com.google.sitebricks.At;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@At("/test")
public class TestPage {
    private String message = "hello";
    private boolean appear = true;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAppear() {
        return appear;
    }

    public void setAppear(boolean appear) {
        this.appear = appear;
    }
}
