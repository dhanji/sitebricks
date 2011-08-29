package com.google.sitebricks.compiler;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.Template;
import com.google.sitebricks.compiler.template.freemarker.FreemarkerTemplateCompiler;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

import javax.servlet.ServletContext;

@Singleton
public class FreemarkerTemplateCompilerFactory implements CompilerFactory {
    private final Provider<ServletContext> context;

    @Inject
    public FreemarkerTemplateCompilerFactory(Provider<ServletContext> context) {
        this.context = context;
    }

    @Override
    public FreemarkerTemplateCompiler get(Class<?> templateClass, Template template, WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics) {
        return new FreemarkerTemplateCompiler(templateClass, context.get());
    }
}
