package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.StringBuilderRespond;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.rendering.SelfRendering;
import net.jcip.annotations.Immutable;

import java.util.Collections;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
@SelfRendering
class RequireWidget implements Renderable {
  private final XmlWidget widget;

  public RequireWidget(XmlWidget child) throws ExpressionCompileException {
    this.widget = child;
  }

  public void render(Object bound, Respond respond) {
    StringBuilderRespond inner = new StringBuilderRespond(bound);
    widget.render(bound, inner);

    //special method interns tokens
    respond.require(inner.toString());
  }

  public <T extends Renderable> Set<T> collect(Class<T> clazz) {
    return Collections.emptySet();
  }
}
