package com.google.sitebricks.rendering.control;

import com.google.inject.Guice;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class EmbeddedRespondExtractorTest {
    private static final String HTMLDOCS_AND_SPLITS = "htmlDocsNSplitz";

    

    public static final String HTML_DOC_SIMPLE = "<html>\n" +
            " @Meta\n" +
            " <head id=\"hd\">\n" +
            "   <title>yoyo</title>\n" +
            " </head>\n" +
            " \n" +
            " <body>\n" +
            "\n\n" +
            " <p>Greetings</p>\n\n" +
            " <input type='as'/>" +
            " Some free text\n</body>" +
            "</html>";

    public static final String HTML_DOC_BODY_WITHATTRS = "<html>\n" +
            " @Meta\n" +
            " <head id=\"hd\">\n" +
            "   <title>yoyo</title>\n" +
            " </head>\n" +
            " \n" +
            " <body id='thing' w:class='dubious' >\n" +
            "\n\n" +
            " <p>Greetings</p>\n\n" +
            " <input type='as'/>" +
            " Some free text\n</body>" +
            "</html>";

    public static final String HTML_DOC_BODY_WITHATTRS_INQUOTES = "<html>\n" +
            " @Meta\n" +
            " <head id=\"hd\">\n" +
            "   <title>yoyo</title>\n" +
            " </head>\n" +
            " \n" +
            " <body id='thing' w:class=\"javascript:call('dubious > jebious')\" >\n" +
            "\n\n" +
            " <p>Greetings</p>\n\n" +
            " <input type='as'/>" +
            " Some free text\n</body>" +
            "</html>";

    public static final String HTML_DOC_HEAD_WITHATTRS_INQUOTES = "<html>\n" +
            " @Meta\n" +
            " <head id=\"hd\" w:class=\"javascript:call('dubious > jebious')\">\n" +
            "   <title>yoyo</title>\n" +
            " </head>\n" +
            " \n" +
            " <body id='thing'  >\n" +
            "\n\n" +
            " <p>Greetings</p>\n\n" +
            " <input type='as'/>" +
            " Some free text\n</body>" +
            "</html>";


    public static final String HTML_DOC_BODY_WITHATTRS_MESSY_QUOTE = "<html>\n" +
            " @Meta\n" +
            " <head id=\"hd>>>>\">\n" +
            "   <title>yoyo</title>\n" +
            " </head>\n" +
            " \n" +
            " @Frame\n" +
            " <body id='thing' w:class='dubious> hubris' >\n" +
            "\n\n" +
            " <p>Greetings</p>\n\n" +
            " @TextField" +
            " <input type='as'/>" +
            " Some free text\n</body>" +
            "</html>";

    public static final String HTML_DOC_HEADLESS = "<html>\n" +
            " @Meta\n" +
            " \n" +
            " \n" +
            " <body>\n" +
            "\n\n" +
            " <p>Greetings</p>\n\n" +
            " <input type='as'/>" +
            " Some free text\n</body>" +
            "</html>";

    public static final String HTML_DOC_SELF_CLOSED_HEAD = "<html>\n" +
            " @Meta\n" +
            " \n<head />" +
            " \n" +
            " <body>\n" +
            "\n\n" +
            " <p>Greetings</p>\n\n" +
            " <input type='as'/>" +
            " Some free text\n</body>" +
            "</html>";

    @DataProvider(name = HTMLDOCS_AND_SPLITS)
    public Object[][] get() {
        return new Object[][] {
            {HTML_DOC_SIMPLE, "<title>yoyo</title>", "<p>Greetings</p>\n\n <input type='as'/> Some free text" },
            {HTML_DOC_BODY_WITHATTRS, "<title>yoyo</title>", "<p>Greetings</p>\n\n <input type='as'/> Some free text" },
            {HTML_DOC_HEAD_WITHATTRS_INQUOTES, "<title>yoyo</title>", "<p>Greetings</p>\n\n <input type='as'/> Some free text" },
            {HTML_DOC_BODY_WITHATTRS_INQUOTES, "<title>yoyo</title>", "<p>Greetings</p>\n\n <input type='as'/> Some free text" },
            {HTML_DOC_BODY_WITHATTRS_MESSY_QUOTE, "<title>yoyo</title>", "<p>Greetings</p>\n\n @TextField <input type='as'/> Some free text" },
            {HTML_DOC_HEADLESS, "", "<p>Greetings</p>\n\n <input type='as'/> Some free text" },
            {HTML_DOC_SELF_CLOSED_HEAD, "", "<p>Greetings</p>\n\n <input type='as'/> Some free text" },
        };
    }

    @Test(dataProvider = HTMLDOCS_AND_SPLITS)
    public final void extractInsideHeadTags(final String htmlDoc, String expectedHead, String expectedBody) {
        final EmbeddedRespondFactory factory = Guice.createInjector().getInstance(EmbeddedRespondFactory.class);

        final EmbeddedRespond respond = factory.get(Collections.<String, ArgumentWidget>emptyMap(), new Object());

        respond.write(htmlDoc);

        final String head = respond.toHeadString();
        final String body = respond.toString();

        assert null != head : "Head was null";
        assert null != body : "body was null";

//        System.out.println("head: " + head);
//        System.out.println("body: " + body);
//        assert "".equals(head.trim()) : "Head did not match : " + head;
        assert expectedBody.equals(body.trim()) : "Body did not match";

    }
}
