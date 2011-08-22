package com.google.sitebricks.compiler;

import com.google.inject.Singleton;
import com.google.sitebricks.Template;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

@Singleton
public class XmlTemplateCompilerFactory implements CompilerFactory {
    @Override
    public XmlTemplateCompiler get(Class<?> templateClass, Template template, WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics) {
        return new XmlTemplateCompiler(templateClass, new MvelEvaluatorCompiler(templateClass), registry, pageBook,
        metrics);
    }
}
