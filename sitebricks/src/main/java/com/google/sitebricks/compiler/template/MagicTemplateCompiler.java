package com.google.sitebricks.compiler.template;

import com.google.sitebricks.Template;

public interface MagicTemplateCompiler {
  Template transform(Template template);
  String process(Class<?> page, Object bound, Template template);
}
