package com.google.sitebricks.compiler;

import com.google.inject.Singleton;
import com.google.sitebricks.Template;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

@Singleton
public class FlatTemplateCompilerFactory implements CompilerFactory {
    @Override
    public FlatTemplateCompiler get(Class<?> templateClass, Template template, WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics) {
        return new FlatTemplateCompiler(templateClass, new MvelEvaluatorCompiler(templateClass), metrics, registry);
    }
}
