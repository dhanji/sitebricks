package com.google.sitebricks.example;

import com.google.sitebricks.rendering.EmbedAs;

import java.util.Arrays;
import java.util.List;

/**
 * author: Martins Barinskis (martins.barinskis@gmail.com)
 */
@EmbedAs("ThreeTestItems")
public class ThreeTestItems {

    private static final List<String> ITEMS = Arrays.asList("first", "second", "third");

    private String suffix;

    public List<String> getItems() {
        return ITEMS;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
