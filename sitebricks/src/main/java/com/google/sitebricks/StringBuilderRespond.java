package com.google.sitebricks;

import net.jcip.annotations.NotThreadSafe;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@NotThreadSafe
public class StringBuilderRespond implements Respond {

  private static final String TEXT_TAG_TEMPLATE = "sitebricks.template.textfield";
  private static final String TEXTAREA_TAG_TEMPLATE = "sitebricks.template.textarea";

  //TODO Improve performance by using an insertion index rather than a placeholder string
  private static final String HEADER_PLACEHOLDER = "__sb:PLACEhOlDeR:__";

  private static final AtomicReference<Map<String, String>> templates =
      new AtomicReference<Map<String, String>>();

  private static final String TEXT_HTML = "text/html;charset=utf-8";
  private Object page;

  @SuppressWarnings("unchecked")
  public StringBuilderRespond(Object context) {
    this.page = context;
    if (null == templates.get()) {
      final Properties properties = new Properties();
      try {
        properties.load(StringBuilderRespond.class.getResourceAsStream("templates.properties"));
      } catch (IOException e) {
        throw new NoSuchResourceException("Can't find templates.properties", e);
      }

      //Concurrent/idempotent
      templates.compareAndSet(null, (Map) properties);
    }
  }

  private final StringBuilder out = new StringBuilder();
  private final StringBuilder head = new StringBuilder();

  //TODO use SortedSet for clustering certain tag types together.
  private final Set<String> requires = new LinkedHashSet<String>();
  private String redirect;

  public String getHead() {
    return head.toString();
  }

  public void write(String text) {
    out.append(text);
  }

  public HtmlTagBuilder withHtml() {
    return new HtmlBuilder();
  }

  public void write(char c) {
    out.append(c);
  }

  public void require(String require) {
    requires.add(require);
  }

  public void redirect(String to) {
    this.redirect = to;
  }

  public void writeToHead(String text) {
    head.append(text);
  }

  public void chew() {
    out.deleteCharAt(out.length() - 1);
  }

  public String getRedirect() {
    return redirect;
  }

  public Renderable include(String argument) {
    return null;
  }

  public String getContentType() {
    return TEXT_HTML;
  }

  public void clear() {
    if (null != out) {
      out.delete(0, out.length());
    }
    if (null != head) {
      head.delete(0, head.length());
    }
  }

  @Override public Object pageObject() {
    return page;
  }

  @Override
  public String toString() {
    //write requires to header first...
    for (String require : requires) {
      writeToHead(require);
    }

    //write header to placeholder...
    //TODO optimize by scanning upto <body> only (if no head)
    int index = out.indexOf(HEADER_PLACEHOLDER);

    String output = out.toString();

    if (index > 0) {
      output = output.replaceFirst(HEADER_PLACEHOLDER, head.toString());
    }

    return output;
  }

  //do NOT make this a static inner class!
  private class HtmlBuilder implements HtmlTagBuilder {

    public void textField(String bind, String value) {
      write(String.format(templates.get().get(TEXT_TAG_TEMPLATE), bind, value));
    }

    public void headerPlaceholder() {
      write(HEADER_PLACEHOLDER);
    }

    public void textArea(String bind, String value) {
      write(String.format(templates.get().get(TEXTAREA_TAG_TEMPLATE), bind, value));
    }
  }
}
