package com.google.sitebricks;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Template {
  
  //
  // templateName like foo.html or bar.fml
  //
  private final String templateName;
  private final String text;
  private final TemplateSource source;
  //
  // The text may be transformed in some way like markdown --> xhtml
  //
  private String transformedText;

  public Template(String text) {
    this(null, text, null);
  }
  
  public Template(String templateName, String text, TemplateSource source) {
    this.templateName = templateName;
    this.text = text;
    this.source = source;
  }

  public String getName() {
    return templateName;
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
}
