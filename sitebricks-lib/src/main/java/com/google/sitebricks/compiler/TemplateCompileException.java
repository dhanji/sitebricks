package com.google.sitebricks.compiler;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public final class TemplateCompileException extends RuntimeException {
    private final List<CompileError> errors;
    private final List<String> templateLines;
    private final String template;
    private final Class<?> page;
    private final List<CompileError> warnings;

    public TemplateCompileException(Class<?> page, String template,
                                    List<CompileError> errors, List<CompileError> warnings) {

        this.page = page;
        this.warnings = warnings;
        try {
            //noinspection unchecked
            this.templateLines = IOUtils.readLines(new StringReader(template));
        } catch (IOException e) {
            throw new IllegalStateException("Fatal error, could not read template after compile", e);
        }
        this.template = template;

        this.errors = errors;
    }


    @Override
    public String getMessage() {
        if (null == errors)
            return super.getMessage();

        StringBuilder builder = new StringBuilder("Compilation errors in template for ");
        builder.append(page.getName());
        builder.append("\n\n");

        AtomicInteger i = new AtomicInteger(0);

        if (!errors.isEmpty()) {
            toString(builder, i, errors);
            builder.append("\nTotal errors: ");
            builder.append(errors.size());
            builder.append("\n\n");
        }

        if (!warnings.isEmpty()) {
            toString(builder, i, warnings);
            builder.append("\nTotal warnings: ");
            builder.append(warnings.size());
            builder.append("\n\n");
        }

        return builder.toString();
    }

    private void toString(StringBuilder builder, AtomicInteger i, List<CompileError> errors) {
        for (CompileError error : errors) {
            EvaluatorCompiler.CompileErrorDetail cause = error.getCause();

            builder.append(i.incrementAndGet());
            builder.append(") ");
            builder.append(cause.getError().getMessage());
            builder.append("\n\n");

            // Context (source code) of the error.
            int lineNumber = error.getLine();
            builder.append(lineNumber - 1);
            builder.append(": ");
            builder.append(templateLines.get(lineNumber - 1));
            builder.append('\n');
            builder.append(lineNumber);
            builder.append(": ");
            builder.append(templateLines.get(lineNumber));
            builder.append('\n');

            // Actual error line...
            int contextLineNumber = lineNumber + 1;
            builder.append(contextLineNumber);
            builder.append(": ");
            String fragment = templateLines.get(contextLineNumber);
            builder.append(fragment);
            builder.append('\n');

            // Compute offset (line number width + expression offset).
            int columnPad = Integer.toString(contextLineNumber).length() + 4;
            int offset = fragment.indexOf(cause.getExpression()) + columnPad;

            // Code pointer (caret).
            // TODO fix this. It should appear directly beneath the line in question.
            char[] spaces = new char[offset];
            Arrays.fill(spaces, ' ');
            builder.append(spaces);
            builder.append("^");

            builder.append('\n');
        }
    }

    public List<CompileError> getErrors() {
        return errors;
    }

    public List<CompileError> getWarnings() {
        return warnings;
    }
}
