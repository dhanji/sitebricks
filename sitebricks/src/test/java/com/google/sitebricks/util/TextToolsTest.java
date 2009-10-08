package com.google.sitebricks.util;

import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.sitebricks.compiler.*;
import com.google.sitebricks.rendering.DynTypedMvelEvaluatorCompiler;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * On: Mar 25, 2007 12:06:10 PM
 *
 * @author Dhanji R. Prasanna (dhanji at gmail com)
 */
public class TextToolsTest {
  private static final String TOKENS = "tokens";


  @DataProvider(name = TOKENS)
  public final Object[][] tokens() {
    return new Object[][]{
        {new String[]{"hello ", "expr", "${expr}"}},
        {new String[]{"hello ", "expr", "${expr}", "as", "$asd", "$ {}"}},
        {new String[]{"$$ { ", "{}", "${}"}},
    };
  }

  @Test(dataProvider = TOKENS)
  public final void testTokenize(String[] rawStream) throws ExpressionCompileException {
    StringBuilder builder = new StringBuilder();
    for (String chunk : rawStream)
      builder.append(chunk);

    List<Token> tokens = Parsing.tokenize(builder.toString(),
        new DynTypedMvelEvaluatorCompiler(new HashMap<String, Class<?>>()));

    assertEquals(tokens.size(),rawStream.length);

    for (int i = 0; i < rawStream.length; i++) {
      Token token = tokens.get(i);
//            assert rawStream[i].equals(token.getToken());

      if (rawStream[i].startsWith("${") && rawStream[i].endsWith("}"))
        assertTrue(token.isExpression());
      else
        assertTrue(!token.isExpression());
    }
  }

}
