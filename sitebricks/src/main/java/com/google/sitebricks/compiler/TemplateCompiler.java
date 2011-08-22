package com.google.sitebricks.compiler;

import com.google.sitebricks.Renderable;

public interface TemplateCompiler {
    public Renderable compile(String template);
}
