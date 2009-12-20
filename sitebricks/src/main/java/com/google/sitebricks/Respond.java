package com.google.sitebricks;

import com.google.inject.ImplementedBy;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(StringBuilderRespond.class)
public interface Respond {
  Respond HEADLESS = new StringBuilderRespond();

  void write(String text);

    HtmlTagBuilder withHtml();

    void write(char c);

    void chew();

    String toString();

    void writeToHead(String text);

    void require(String requireString);

    void redirect(String to);

    String getContentType();

    String getRedirect();

    Renderable include(String argument);

    String getHead();

    public static interface HtmlTagBuilder {
        void textField(String value, String s);

        void headerPlaceholder();

        void textArea(String expression, String s);
    }
}
