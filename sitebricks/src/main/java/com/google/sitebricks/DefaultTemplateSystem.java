package com.google.sitebricks;

public class DefaultTemplateSystem implements TemplateSystem {

  @Override
  public String[] getTemplateExtensions() {
    return new String[] { "%s.html", "%s.xhtml", "%s.xml", "%s.txt", "%s.fml", "%s.dml", "%s.mvel" };
  }
}