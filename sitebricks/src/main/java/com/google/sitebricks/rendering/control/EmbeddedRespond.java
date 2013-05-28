package com.google.sitebricks.rendering.control;

import java.util.List;
import java.util.Map;

import com.google.sitebricks.Respond;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
class EmbeddedRespond implements Respond {
  private static final String BODY_BEGIN = "<body";
  private static final String BODY_END = "</body>";
  private static final char NOT_IN_QUOTE = '\0';

  // Memo fields.
  private String body;

  private final Map<String, ArgumentWidget> arguments;
  private final Respond delegate;
  
  private List<String> errors;
  
  public EmbeddedRespond(Map<String, ArgumentWidget> arguments, Respond respond) {
    this.arguments = arguments;
    this.delegate = respond;
  }

  public String toHeadString() {
    if (null == body) {
      //extract and store
      extract(delegate.toString());
    }

    //we discard the <head> tag rendered in the child page and
    //instead return only what was directly rendered with writeToHead()
    return delegate.getHead();
  }

  //state machine extracts <body> tag content

  private void extract(String htmlDoc) {

    //now extract the contents of <body>...
    int bodyStart = htmlDoc.indexOf(BODY_BEGIN) + BODY_BEGIN.length();

    //scan for end of the <body> start tag (beginning of body content)
    char quote = NOT_IN_QUOTE;
    for (int body = bodyStart; body < htmlDoc.length(); body++) {
      final char c = htmlDoc.charAt(body);
      if (isQuoteChar(c)) {
        if (quote == NOT_IN_QUOTE)
          quote = c;
        else if (quote == c)
          quote = NOT_IN_QUOTE;
      }

      if ('>' == c && NOT_IN_QUOTE == quote) {
        bodyStart = body + 1;
        break;
      }
    }

    int bodyEnd = htmlDoc.indexOf(BODY_END, bodyStart);

    //if there was no body tag, just embed whatever was rendered directly
    if (-1 == bodyEnd) {
      EmbeddedRespond.this.body = htmlDoc;
    } else
      EmbeddedRespond.this.body = htmlDoc.substring(bodyStart, bodyEnd);
  }


  private static boolean isQuoteChar(char c) {
    return '"' == c || '\'' == c;
  }

  public void write(String text) {
    delegate.write(text);
  }

  public HtmlTagBuilder withHtml() {
    return delegate.withHtml();
  }

  public void write(char c) {
    delegate.write(c);
  }

  public void require(String require) {
    delegate.require(require);
  }

  public void redirect(String to) {
    delegate.redirect(to);
  }

  public void writeToHead(String text) {
    delegate.writeToHead(text);
  }

  public void chew() {
    delegate.chew();
  }

  public String getRedirect() {
    return delegate.getRedirect();
  }


  public String getContentType() {
    return delegate.getContentType();
  }

  public ArgumentWidget include(String name) {
    return arguments.get(name);
  }

  public String getHead() {
    return delegate.getHead();
  }

  public void clear() {
    delegate.clear();
  }

  @Override public Object pageObject() {
    return delegate.pageObject();
  }

  @Override
  public List<String> getErrors() {
    return this.errors;
  }

  @Override
  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  @Override
  public String toString() {
    if (null == body) {
      extract(super.toString());
    }

    return body;
  }
}
