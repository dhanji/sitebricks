package com.google.sitebricks.compiler;

import com.google.sitebricks.Template;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

public interface CompilerFactory {
    public TemplateCompiler get(Class<?> templateClass, Template template, WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics);

    //public void registered(String extension);
}
