package com.google.sitebricks.example;

import com.google.sitebricks.At;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@At("/case")
public class Case {
    private String color = "green";

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
