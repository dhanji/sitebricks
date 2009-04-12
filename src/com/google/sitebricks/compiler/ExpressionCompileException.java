package com.google.sitebricks.compiler;

import org.mvel.ErrorDetail;

import java.util.List;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public final class ExpressionCompileException extends Throwable {
    private final List<ErrorDetail> errors;
    private final String expression;

    public ExpressionCompileException(String expression, List<ErrorDetail> errors) {
        this.errors = errors;
        this.expression = expression;
    }

    public ExpressionCompileException(String msg) {
        super(msg);

        expression = null;
        errors = null;
    }


    public EvaluatorCompiler.CompileErrorDetail getError() {
        //TODO is it enough to report just the first error?
        //ensure we wrap this in ${}
        return new EvaluatorCompiler.CompileErrorDetail(String.format("${%s}", expression), new ErrorDetail(expression, true));
    }
}
