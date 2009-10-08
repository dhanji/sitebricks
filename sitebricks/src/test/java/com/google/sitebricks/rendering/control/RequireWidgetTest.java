package com.google.sitebricks.rendering.control;

import com.google.common.collect.Maps;
import com.google.sitebricks.Respond;
import com.google.sitebricks.RespondersForTesting;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.compiler.MvelEvaluatorCompiler;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class RequireWidgetTest {
  private static final String REQUIRE_TAGS = "requireTags";

  @DataProvider(name = REQUIRE_TAGS)
  public Object[][] getRequires() {
    return new Object[][]{
        {"<link rel='thing.css'/>"},
        {"<script type=\"text/javascript\"/> @import 'thing.css' javascript dude bleod </script>"},
    };
  }

  @Test(dataProvider = REQUIRE_TAGS)
  public final void requireWidgetsRenderToHeadTag(final String requireString)
      throws ExpressionCompileException {
    final Respond respond = RespondersForTesting.newRespond();

    respond.require(requireString);
    respond.require(requireString);

    WidgetChain chain = new ProceedingWidgetChain();
    final MvelEvaluatorCompiler compiler = new MvelEvaluatorCompiler(Object.class);

    chain.addWidget(new HeaderWidget(new TerminalWidgetChain(),
        Maps.<String, String>newHashMap(), compiler));

    chain.addWidget(new RequireWidget(requireString, compiler));
    chain.addWidget(new RequireWidget(requireString, compiler));
    chain.addWidget(new RequireWidget(requireString, compiler));

    //render
    chain.render(new Object(), respond);

    final String expected = "<head>" + requireString + "</head>";
    final String output = respond.toString();
    assert expected.equals(output) : "Header not correctly rendered: " + output;
  }
}
