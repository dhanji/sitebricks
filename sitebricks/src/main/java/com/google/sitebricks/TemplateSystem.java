package com.google.sitebricks;

import com.google.sitebricks.compiler.TemplateCompiler;

public interface TemplateSystem {
  String[] getTemplateExtensions();
  TemplateCompiler compilerFor(String templateName);  
}
