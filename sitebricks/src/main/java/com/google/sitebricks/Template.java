package com.google.sitebricks;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Template {
  private final Kind templateKind;
  private final String text;
  private final TemplateSource source;

  public Template(Kind templateKind, String text, TemplateSource source) {
    this.templateKind = templateKind;
    this.text = text;
    this.source = source;
  }

  public Kind getKind() {
    return templateKind;
  }

  public String getText() {
    return text;
  }

  public TemplateSource getTemplateSource() {
    return source;
  }
  
  public static enum Kind {
    HTML, XML, FLAT, MVEL, FREEMARKER, FREEMARKER_DECORATOR;

    /**
     * Returns whether a given template should be treated as html, xml or flat
     * (currently by looking at file extension)
     */
    public static Kind kindOf(String template) {
      if (template.endsWith(".html") || template.endsWith(".xhtml"))
        return HTML;
      else if (template.endsWith(".xml"))
        return XML;
      else if (template.endsWith(".mvel"))
        return MVEL;
      else if (template.endsWith(".fml"))
        return FREEMARKER;
      else if (template.endsWith(".dml"))
        return FREEMARKER_DECORATOR;
      else
        return FLAT;
    }

  }
}
