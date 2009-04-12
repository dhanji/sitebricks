package com.google.sitebricks.compiler;

import com.google.sitebricks.Evaluator;
import net.jcip.annotations.Immutable;

/**
 * Created with IntelliJ IDEA.
 * On: 20/03/2007
 *
 * A simple wrapper around a string or expression (with evaluator), denoting it as a
 *  renderable token.
 *
 * @author Dhanji R. Prasanna (dhanji at gmail com)
 * @since 1.0
 */
@Immutable
class CompiledToken implements Token {
    private final String token;
    private final boolean isExpression;
    private final Evaluator evaluator;

    private CompiledToken(String token, boolean expression) {
        this.token = token;
        this.evaluator = null;
        isExpression = expression;
    }

    private CompiledToken(Evaluator evaluator, boolean expression) {
        this.evaluator = evaluator;
        isExpression = expression;
        this.token = null;
    }

    public boolean isExpression() {
        return isExpression;
    }

    public Object render(Object bound) {
        return isExpression ? evaluator.evaluate(null, bound) : token;
    }

    //local factories
    static CompiledToken expression(String token, EvaluatorCompiler compiler) throws ExpressionCompileException {
        //strip leading ${ and trailing }
        return new CompiledToken(compiler.compile(token.substring(2, token.length() - 1)), true);
    }

    static CompiledToken text(String token) {
        return new CompiledToken(token, false);
    }
}
