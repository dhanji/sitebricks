package com.google.sitebricks.rendering.resource;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class MimeTypesRegexIntegrationTest {
    private static final String MIMES_AND_FILES = "mimesAndFiles";

    @DataProvider(name = MIMES_AND_FILES)
    public Object[][] get() {
        return new Object[][] {
            { "/thing/holy.js", "text/javascript" },
            { "/thing/%20blah.thingaly.xml", "text/xml" },
            { "/thing/%20blah.thingalyxml", "text/plain" },     //default
            { "/thing/holy.js/nekkid.png", "image/png" },
        };
    }

    @Test(dataProvider = MIMES_AND_FILES)
    public final void mimeTypeMatching(final String file, final String mimeType) throws IOException {
        new ClasspathResourcesService();    //impure call loads mimetypes from classpath

        final String mime = ClasspathResourcesService.mimeOf(file);
        assert mimeType.equals(mime) : "Did not match, instead was: " + mime;
    }
}
