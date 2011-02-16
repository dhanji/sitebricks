package com.google.sitebricks.compiler;

import com.google.sitebricks.binding.TypeConverter;

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
     *  context object. Values are converted to String using the {@code TypeConverter}.
     *
     */
    String render(Object bound);
}
