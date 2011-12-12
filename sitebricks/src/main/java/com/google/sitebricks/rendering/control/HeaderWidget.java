package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.compiler.EvaluatorCompiler;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.compiler.Token;
import com.google.sitebricks.rendering.SelfRendering;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@SelfRendering
class HeaderWidget implements Renderable {
  private final WidgetChain widgetChain;
  private Map<String, List<Token>> attribs;

  public HeaderWidget(WidgetChain widgetChain, Map<String, String> attribs,
                      EvaluatorCompiler compiler) throws ExpressionCompileException {

    this.widgetChain = widgetChain;
    this.attribs = XmlWidget.compile(attribs, compiler);
  }

  public void render(Object bound, Respond respond) {
    XmlWidget.writeOpenTag(bound, respond, "head", attribs);

    respond.write('>');

    //render children (as necessary)
    widgetChain.render(bound, respond);

    respond.withHtml()
        .headerPlaceholder(); //TODO replace placeholder with an index?
    respond.write("</head>");
  }


  public <T extends Renderable> Set<T> collect(Class<T> clazz) {
    return widgetChain.collect(clazz);
  }
}
