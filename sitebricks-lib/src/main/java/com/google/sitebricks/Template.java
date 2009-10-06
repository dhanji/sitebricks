package com.google.sitebricks;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
class Template {
  private final Kind templateKind;
  private final String text;

  public Template(Kind templateKind, String text) {
    this.templateKind = templateKind;
    this.text = text;
  }

  public Kind getKind() {
    return templateKind;
  }

  public String getText() {
    return text;
  }

  public static enum Kind {
    XML, FLAT,;

    /**
     * Returns whether or not the given filename for a template should be
     * treated as xml or not. 
     */
    public static Kind kindOf(String template) {
      return (template.endsWith(".xml") || template.endsWith(".html")
          || template.endsWith(".xhtml")) ? XML : FLAT;
    }
  }
}
