package com.google.sitebricks.compiler;

/**
 * Represents a compiled, evaluable expression or raw String token.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface Token {

    /**
     *
     * @return Returns true if this is an evaluable expression (usually with an embedded
     *  MVEL evaluator).
     *
     */
    boolean isExpression();

    /**
     *
     * @param bound A context object to evaluate against (must matched the compiled context
     *  class of this expression token).
     *
     * @return Returns the result of evaluating the expression token against the provided
     *  context object. Typically you can call #toString() on the returned object to render
     *  it in a response stream. If this token is a raw string, the returned object is the
     *  raw string.
     *
     */
    Object render(Object bound);
}
