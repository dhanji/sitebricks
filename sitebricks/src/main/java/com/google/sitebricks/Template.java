package com.google.sitebricks;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Template {
  private final Kind templateKind;
  private final String text;
  private final TemplateSource source;
  //
  // The text may be transformed in some way like markdown --> xhtml
  //
  private String transformedText;

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
    
  public String getTransformedText() {
    return transformedText;
  }

  public void setTransformedText(String transformedText) {
    this.transformedText = transformedText;
  }

  // This should be removed and the kind should not be hardcoded.
  
  public static enum Kind {
    HTML, XML, FLAT, MVEL, FREEMARKER, MAGIC;

    /**
     * Returns whether a given template should be treated as html, xml or flat (currently by looking at file extension)
     */
    public static Kind kindOf(String template) {
      if (template.startsWith("m_") || template.endsWith(".dml")) {
        return MAGIC;
      } else if (template.endsWith(".html") || template.endsWith(".xhtml"))
        return HTML;
      else if (template.endsWith(".xml"))
        return XML;
      else if (template.endsWith(".mvel"))
        return MVEL;
      else if (template.endsWith(".fml"))
        return FREEMARKER;
      else
        return FLAT;
    }
  }
}
