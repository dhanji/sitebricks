package com.google.sitebricks.rendering.control;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.compiler.EvaluatorCompiler;
import com.google.sitebricks.compiler.ExpressionCompileException;
import com.google.sitebricks.compiler.Token;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.rendering.Attributes;
import com.google.sitebricks.rendering.SelfRendering;
import net.jcip.annotations.ThreadSafe;

import java.util.*;

/**
 * <p> Widget renders an XML-like tag </p>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe
@SelfRendering
class XmlWidget implements Renderable {
  private final WidgetChain widgetChain;
  private final boolean selfClosed;
  private final String name;
  private final Map<String, List<Token>> attributes;

  // HACK Extremely ouch! Replace with Assisted inject.
  private static volatile Provider<Request> request;

  private static final Set<String> CONTEXTUAL_ATTRIBS;

  static {
    Set<String> set = new HashSet<String>();

    set.add("href");
    set.add("action");
    set.add("src");

    CONTEXTUAL_ATTRIBS = Collections.unmodifiableSet(set);
  }


  XmlWidget(WidgetChain widgetChain, String name, EvaluatorCompiler compiler,
            @Attributes Map<String, String> attributes) throws ExpressionCompileException {
    this.widgetChain = widgetChain;
    this.name = name;
    this.attributes = Collections.unmodifiableMap(compile(attributes, compiler));

    //hacky. Script tags should not be self-closed due to IE insanity.
    this.selfClosed =
        widgetChain instanceof TerminalWidgetChain && !"script".equalsIgnoreCase(name);
  }

  //compiles a map of name:value attrs into a map of name:token renderables
  static Map<String, List<Token>> compile(Map<String, String> attributes,
                                          EvaluatorCompiler compiler)
      throws ExpressionCompileException {

    Map<String, List<Token>> map = new LinkedHashMap<String, List<Token>>();

    for (Map.Entry<String, String> attribute : attributes.entrySet()) {
      map.put(attribute.getKey(), compiler.tokenizeAndCompile(attribute.getValue()));
    }

    return map;
  }

  public void render(Object bound, Respond respond) {
    writeOpenTag(bound, respond, name, attributes);

    //write children
    if (selfClosed) {
      respond.write("/>");    //write self-closed tag
    } else {
      respond.write('>');
      widgetChain.render(bound, respond);

      //close tag
      respond.write("</");
      respond.write(name);
      respond.write('>');
    }
  }

  static void writeOpenTag(Object bound, Respond respond, String name,
                           Map<String, List<Token>> attributes) {
    respond.write('<');
    respond.write(name);

    respond.write(' ');

    //write attributes
    for (Map.Entry<String, List<Token>> attribute : attributes.entrySet()) {
      respond.write(attribute.getKey());
      respond.write("=\"");

      final List<Token> tokenList = attribute.getValue();
      for (int i = 0; i < tokenList.size(); i++) {
        Token token = tokenList.get(i);

        if (token.isExpression()) {
          respond.write(token.render(bound));
        } else {
          respond.write(
              contextualizeIfNeeded(attribute.getKey(), (0 == i), token.render(bound)));
        }
      }

      respond.write("\" ");
    }

    respond.chew();
  }

  static String contextualizeIfNeeded(String attribute, boolean isFirstToken, String raw) {
    if (isFirstToken && CONTEXTUAL_ATTRIBS.contains(attribute)) {
      //add context to path if needed
      if (raw.startsWith("/")) {
        if (!raw.startsWith("//"))    // Ignore protocol-relative paths.
          raw = request.get().context() + raw;
      }
    }

    return raw;
  }


  public <T extends Renderable> Set<T> collect(Class<T> clazz) {
    return widgetChain.collect(clazz);
  }

  @Inject
  public void setRequestProvider(Provider<Request> requestProvider) {
    XmlWidget.request = requestProvider;
  }
}