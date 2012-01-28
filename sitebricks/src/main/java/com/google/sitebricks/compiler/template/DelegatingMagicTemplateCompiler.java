package com.google.sitebricks.compiler.template;

import com.google.sitebricks.Template;

public class DelegatingMagicTemplateCompiler implements MagicTemplateCompiler {

  @Override
  public Template transform(Template template) {
    return null;
  }

  @Override
  public String process(Object bound, Template template) {
    return null;
  }
}
