package com.google.sitebricks.example;

import com.google.sitebricks.At;
import com.google.sitebricks.Show;

import java.util.*;

/**
 * 
 */
@At("/jsp")
@Show("Jsp.jsp")
public class Jsp {
    private static final List<String> NAMES = Arrays.asList("Dhanji", "Josh", "Jody", "Iron Man");

    //property returns a list of names
    public List<String> getNames() {
        return NAMES;
    }

    //try a set this time, returns movies (to demo nested repeat)
    public Set<Movie> getMovies() {
        return new HashSet<Movie>(Arrays.asList(new Movie(), new Movie(), new Movie()));
    }

    public static class Movie {

        //try a collection this time. same as property Repeat.getNames() from the outer class
        public Collection<String> getActors() {
            return NAMES;
        }
    }
}