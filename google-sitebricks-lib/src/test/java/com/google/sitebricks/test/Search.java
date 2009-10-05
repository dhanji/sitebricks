package com.google.sitebricks.test;

import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Select;

import java.util.Collection;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@At("/wiki/search") @Select("event")
public class Search {   //defaults to @Show("Search.xhtml"), or @Show("Search.html")

    private int counter;
    private String query;   //"get" param
    private Collection<Movie> movies;

    public Collection<Movie> getMovies() {
        return movies;
    }


    public static class Movie {
        public String getMovieName() {
            return "thing";
        }

    }

    @Get("results")
    public void showResults() { //called after parameters are bound
    }


    //how about a search bar widget?
    @Get("widget")
    public void showSearchWidget() {
        //don't need to do anything but you could set up some contextual info on the widget here
    }

    public int getCounter() {
        return counter;
    }

    public String getQuery() {
        return query;
    }
}
