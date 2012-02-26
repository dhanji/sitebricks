package com.google.sitebricks;

import java.util.Map;

import com.google.inject.Inject;
import com.google.sitebricks.compiler.TemplateCompiler;

public class DefaultTemplateSystem implements TemplateSystem {

  private Map<String, TemplateCompiler> templateCompilers;

  @Inject
  public DefaultTemplateSystem(Map<String, TemplateCompiler> templateCompilers) {
    this.templateCompilers = templateCompilers;
  }

  public TemplateCompiler compilerFor(String templateName) {
    String extension = templateName.substring(templateName.lastIndexOf(".") + 1);
    TemplateCompiler templateCompiler = templateCompilers.get(extension);
    return templateCompiler;    
  }

  @Override
  public String[] getTemplateExtensions() {
    return new String[] { "%s.html", "%s.xhtml", "%s.xml", "%s.txt", "%s.fml", "%s.dml", "%s.mvel" };
  }  
}