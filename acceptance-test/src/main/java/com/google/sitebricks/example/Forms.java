package com.google.sitebricks.example;

import com.google.sitebricks.At;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@At("/forms")
public class Forms {
    private String text = "initial textfield value";
    private String chosen = "(nothing)";
    private List<String> autobots = Arrays.asList("Bumblebee", "Ultra Magnus", "Optimus Prime", "Kup", "Hot Rod");

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getAutobots() {
        return autobots;
    }

    public void setAutobots(List<String> autobots) {
        this.autobots = autobots;
    }

    public String getChosen() {
        return chosen;
    }

    public void setChosen(String chosen) {
        this.chosen = chosen;
    }
}