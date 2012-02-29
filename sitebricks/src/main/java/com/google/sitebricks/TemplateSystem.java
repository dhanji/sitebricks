package com.google.sitebricks;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.compiler.TemplateCompiler;

@ImplementedBy(DefaultTemplateSystem.class)
public interface TemplateSystem {
  String[] getTemplateExtensions();
  TemplateCompiler compilerFor(String templateName);  
}
