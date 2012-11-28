package com.google.sitebricks.example;

import com.google.sitebricks.rendering.Decorated;

import java.util.Arrays;
import java.util.List;

/**
 * author: Martins Barinskis (martins.barinskis@gmail.com)
 */
@Decorated
public class DecoratedRepeat extends DecoratorPage {

    private static final List<String> ITEMS = Arrays.asList("one", "two", "three");

    private final String messagePrefix = "Hello, ";

    public String getMessagePrefix() {
        return messagePrefix;
    }

    public List<String> getItems() {
        return ITEMS;
    }

    @Override
    public String getWorld() {
        return "This is a decorated page with a repeat block";
    }
}
