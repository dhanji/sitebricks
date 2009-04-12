package com.google.sitebricks;

import com.google.inject.ImplementedBy;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(MvelEvaluator.class)
public interface Evaluator {
    @Nullable
    Object evaluate(String expr, Object bean);

    void write(String expr, Object bean, Object value);

    Object read(String property, Object contextObject);
}
