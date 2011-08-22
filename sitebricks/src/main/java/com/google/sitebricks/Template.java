package com.google.sitebricks;

import org.apache.commons.lang.StringUtils;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Template {
    private String template;
    private final String text;

    public Template(String template, String text) {
    this.template = template;
    this.text = text;
  }

  public String getText() {
    return text;
  }

    public String getExtension() {
        return StringUtils.lowerCase(StringUtils.substringAfterLast(template, "."));
    }
}
