package com.google.sitebricks.compiler;

import com.google.inject.Inject;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Template;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.SystemMetrics;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Compiles non-XML templates.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 * @see XmlTemplateCompiler
 */
public class FlatTemplateCompiler implements TemplateCompiler {
    private final SystemMetrics metrics;
    private final WidgetRegistry registry;

    @Inject
    public FlatTemplateCompiler(SystemMetrics metrics, WidgetRegistry registry) {
        this.metrics = metrics;
        this.registry = registry;
    }

    public Renderable compile(Class<?> page, Template template) {
      
        try {
            return registry.textWidget(template.getText(), new MvelEvaluatorCompiler(page));

        } catch (ExpressionCompileException e) {
            final List<CompileError> errors = Arrays.asList(
                    CompileError.in(template.getText())
                            .near(e.getError().getError().getLineNumber())
                            .causedBy(e)
            );

            final List<CompileError> warnings = Collections.emptyList();

            //log errors and abort compile
            metrics.logErrorsAndWarnings(page, errors, warnings);

            throw new TemplateCompileException(page, template.getText(), errors, warnings);
        }
    }
}
