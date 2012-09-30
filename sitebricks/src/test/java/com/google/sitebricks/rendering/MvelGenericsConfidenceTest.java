package com.google.sitebricks.rendering;

import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.CompiledExpression;
import org.mvel2.compiler.ExpressionCompiler;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class MvelGenericsConfidenceTest {
    private static final List<String> STRINGS = Arrays.asList("hi", "there");

    @Test
    public final void determineEgressParametricType() {
        final ParserContext parserContext = new ParserContext();
        parserContext.setStrongTyping(true);
        parserContext.addInput("strings", List.class, new Class[] { String.class });

      ExpressionCompiler compiler = new ExpressionCompiler("strings", parserContext);
      final CompiledExpression expr = compiler
                .compile();

        assert STRINGS.equals(MVEL.executeExpression(expr, new A())) : "faulty expression eval";

        final Type[] typeParameters = compiler.getParserContextState().getLastTypeParameters();

        assert null != typeParameters : "no generic egress type";
        assert String.class.equals(typeParameters[0]) : "wrong generic egress type";
    }
   
    @Test
    public final void determineEgressParametricTypeInExprChain() {
        final ParserContext parserContext = new ParserContext();
        parserContext.setStrongTyping(true);
        parserContext.addInput("strings", A.class);

      ExpressionCompiler compiler = new ExpressionCompiler("strings.strings",
          parserContext);
      final CompiledExpression expr = compiler
                .compile();

        assert STRINGS.equals(MVEL.executeExpression(expr, new B())) : "faulty expression eval";

        final Type[] typeParameters = compiler.getParserContextState().getLastTypeParameters();

        assert null != typeParameters : "no generic egress type";
        assert String.class.equals(typeParameters[0]) : "wrong generic egress type";

    }

    public static class A {

        public List<String> getStrings() {
            return STRINGS;
        }
    }

    public static class B {

        public A getStrings() {
            return new A();
        }
    }
}
