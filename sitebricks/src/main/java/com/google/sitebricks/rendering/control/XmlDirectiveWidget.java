package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.compiler.EvaluatorCompiler;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.compiler.Parsing;
import com.google.sitebricks.compiler.Token;
import com.google.sitebricks.rendering.SelfRendering;
import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * For stuff like Doctype decls at the top of a file.
 */
@ThreadSafe
@SelfRendering
class XmlDirectiveWidget implements Renderable {
  private final List<Token> tokens;

  XmlDirectiveWidget(String template, EvaluatorCompiler compiler) throws ExpressionCompileException {
    this.tokens = Parsing.tokenize(template, compiler);
  }

  public void render(Object bound, Respond respond) {
    respond.write("<!");
    for (Token token : tokens) {
      respond.write(token.render(bound).toString());
    }
    respond.write(">");
  }

  public <T extends Renderable> Set<T> collect(Class<T> clazz) {
    return Collections.emptySet();
  }
}