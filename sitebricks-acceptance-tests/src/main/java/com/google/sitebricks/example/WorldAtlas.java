package com.google.sitebricks.example;

import com.google.sitebricks.rendering.EmbedAs;

import java.util.Arrays;
import java.util.List;

/**
 * author: Martins Barinskis (martins.barinskis@gmail.com)
 */
@EmbedAs("WorldAtlas")
public class WorldAtlas {

    private static final List<Continent> CONTINENTS = Arrays.asList(Continent.EUROPE, Continent.ASIA, Continent.NORTH_AMERICA);

    private String continentPrefix;
    private String countryPrefix;

    public List<Continent> getContinents() {
        return CONTINENTS;
    }

    public String getContinentPrefix() {
        return continentPrefix;
    }

    public void setContinentPrefix(String suffix) {
        this.continentPrefix = suffix;
    }

    public String getCountryPrefix() {
        return countryPrefix;
    }

    public void setCountryPrefix(String countryPrefix) {
        this.countryPrefix = countryPrefix;
    }

    public static enum Continent {

        EUROPE("Europe", Arrays.asList("Germany", "United Kingdom", "France")),
        ASIA("Asia", Arrays.asList("Japan", "China")),
        NORTH_AMERICA("North America", Arrays.asList("United States", "Canada"));


        private final String name;

        private final List<String> countries;

        Continent(String name, List<String> countries) {
            this.name = name;
            this.countries = countries;
        }

        public String getName() {
            return name;
        }

        public List<String> getCountries() {
            return countries;
        }
    }
}
