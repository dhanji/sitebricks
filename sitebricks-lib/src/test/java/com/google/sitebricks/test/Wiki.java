package com.google.sitebricks.test;

import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.Show;
import com.google.sitebricks.rendering.EmbedAs;
import com.google.sitebricks.rendering.resource.Assets;
import com.google.sitebricks.Export;

/**
 *
 */
@At("/wiki/page/:title")
@Show("Wiki.html") 
@EmbedAs("Wiki")
@Assets({@Export(at = "/your.js", resource = "your.js")})
public class Wiki {
    private String title;
    private String language;    //"get" variable, bound by request parameter of same name, via setter
    private String text;        //"post" variable, bound similarly
    private int counter;

    @Get
    public void showPage(@Named("title") String title) {    //URI-part extraction
//        this.title = wikiFind.fetch(title);
        //etc.

        //page is now rendered with the default view
    }

    public int getCounter() {
        return counter;
    }
}
