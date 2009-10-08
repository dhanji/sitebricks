package com.google.sitebricks.rendering.control;

import com.google.common.collect.Maps;
import com.google.sitebricks.Respond;
import com.google.sitebricks.RespondersForTesting;
import com.google.sitebricks.compiler.EvaluatorCompiler;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.compiler.MvelEvaluatorCompiler;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class HeaderWidgetTest {
  private static final String EXPRESSIONS_AND_EVALS = "expressionsAndEvals";

  @DataProvider(name = EXPRESSIONS_AND_EVALS)
  public Object[][] getExprs() {
    return new Object[][]{
        {"visible", true},
        {"!visible", false},
        {"true", true},
        {"false", false},
    };
  }

  @Test
  public final void renderHeader() throws ExpressionCompileException {

    Respond respond = RespondersForTesting.newRespond();

    MvelEvaluatorCompiler compiler = new MvelEvaluatorCompiler(Object.class);
    new HeaderWidget(new ProceedingWidgetChain(), Maps.<String, String>newHashMap(), compiler)
        .render(new Object(), respond);

    respond.writeToHead("<title>bs</title>");

    final String response = respond.toString();
    assert "<head><title>bs</title></head>".equals(response) :
        "instead printed: " + response;
  }

  @Test
  public final void renderHeaderWithContent() throws ExpressionCompileException {

    Respond respond = RespondersForTesting.newRespond();

    final WidgetChain widgetChain = new ProceedingWidgetChain();
    final EvaluatorCompiler mock = new MvelEvaluatorCompiler(Object.class);
    widgetChain.addWidget(new TextWidget("<meta name=\"thing\"/>", mock));

    MvelEvaluatorCompiler compiler = new MvelEvaluatorCompiler(Object.class);

    new HeaderWidget(widgetChain, Maps.<String, String>newHashMap(), compiler)
        .render(new Object(), respond);

    respond.writeToHead("<title>bs</title>");

    final String response = respond.toString();
    assert "<head><meta name=\"thing\"/><title>bs</title></head>".equals(response) :
        "instead printed: " + response;
  }
}