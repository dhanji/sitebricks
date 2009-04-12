package com.google.sitebricks.example;

import com.google.sitebricks.At;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@At("/showif")
public class ShowIf {
    private boolean show;
    private String message = "Hello from google-sitebricks!";

    public String getMessage() {
        return message;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }
}