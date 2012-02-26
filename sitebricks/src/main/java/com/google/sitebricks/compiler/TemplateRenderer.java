package com.google.sitebricks.compiler;

import com.google.sitebricks.Renderable;
import com.google.sitebricks.Template;

public interface TemplateRenderer {
  Renderable compile(Template template);
}
