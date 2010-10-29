package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.compiler.EvaluatorCompiler;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.rendering.SelfRendering;
import net.jcip.annotations.ThreadSafe;

import java.util.Collections;
import java.util.Set;

@ThreadSafe @SelfRendering
class RawTextWidget implements Renderable {
  private String template; //TODO store some metrics to allocate buffers later

  RawTextWidget(String template, EvaluatorCompiler compiler) throws ExpressionCompileException {
    this.template = template;
  }

  public void render(Object bound, Respond respond) {
    respond.write(template);
  }

  public <T extends Renderable> Set<T> collect(Class<T> clazz) {
    return Collections.emptySet();
  }
}