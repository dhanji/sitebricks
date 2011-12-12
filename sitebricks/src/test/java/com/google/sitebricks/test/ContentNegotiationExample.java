package com.google.sitebricks.test;

import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Select;
import com.google.sitebricks.Show;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@At("/aPage") @Show("Wiki.html") @Select("Accept")
public class ContentNegotiationExample {
    private int counter;

    @Get("text/plain") @Show("text.txt")    //Does not work yet!!!
    public void textPage() {

    }

    @Get("text/html") @Show("text.html")    //Does not work yet!!!
    public void htmlPage() {

    }

    public int getCounter() {
        return counter;
    }
}
