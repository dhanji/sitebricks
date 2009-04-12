package com.google.sitebricks.rendering;

import com.google.sitebricks.Evaluator;
import com.google.sitebricks.compiler.EvaluatorCompiler;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.compiler.Parsing;
import com.google.sitebricks.compiler.Token;

import org.jetbrains.annotations.Nullable;
import org.mvel.MVEL;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Temporary class to enable dynamic typing for collection projections (since we don't have
 * a good mechanism in MVEL to reflect on parametric types yet)
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class DynTypedMvelEvaluatorCompiler implements EvaluatorCompiler {

    public DynTypedMvelEvaluatorCompiler(Map<String, Class<?>> map) {
    }

    public Evaluator compile(final String expression) throws ExpressionCompileException {
        return new Evaluator() {
            private final ConcurrentMap<String, Serializable> map = new ConcurrentHashMap<String, Serializable>();

            @Nullable
            public Object evaluate(String ___expr, Object bean) {
                Serializable serializable = map.get(expression);

                if (null == serializable) {
                    serializable = MVEL.compileExpression(expression);
                    map.put(expression, serializable);
                }
                
                return MVEL.executeExpression(serializable, bean);
            }

            public void write(String expr, Object bean, Object value) {
            }

            public Object read(String property, Object contextObject) {
                return MVEL.getProperty(property, contextObject);
            }
        };
    }

    public Class<?> resolveCollectionTypeParameter(String expression) throws ExpressionCompileException {
        return Object.class;
    }

    public List<Token> tokenizeAndCompile(String template) throws ExpressionCompileException {
        return Parsing.tokenize(template, this);
    }

    public Class<?> resolveEgressType(String expression) throws ExpressionCompileException {
        return Collection.class;
    }

  public boolean isWritable(String property) throws ExpressionCompileException {
    return true;
  }
}
