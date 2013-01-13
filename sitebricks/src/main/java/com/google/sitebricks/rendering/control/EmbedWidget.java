package com.google.sitebricks.rendering.control;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.Evaluator;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.compiler.Parsing;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.routing.PageBook;
import net.jcip.annotations.Immutable;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
class EmbedWidget implements Renderable {
  private final Map<String, String> bindExpressions;
  private final Map<String, ArgumentWidget> arguments;
  private final Evaluator evaluator;
  private final PageBook pageBook;
  private final String targetPage;

  private EmbeddedRespondFactory factory;
  private Provider<Request> request;

  public EmbedWidget(Map<String, ArgumentWidget> arguments, String expression,
                     Evaluator evaluator, PageBook pageBook, String targetPage) {
    this.arguments = arguments;

    this.evaluator = evaluator;
    this.pageBook = pageBook;
    this.targetPage = targetPage.toLowerCase();

    //parse expression list
    this.bindExpressions = Parsing.toBindMap(expression);
  }


  public void render(Object bound, Respond respond) {
    PageBook.Page page = pageBook.forName(targetPage);

    //create an instance of the embedded page
    Object pageObject = page.instantiate();

    //bind parameters to it as necessary
    for (Map.Entry<String, String> entry : bindExpressions.entrySet()) {
      evaluator.write(entry.getKey(), pageObject, evaluator.evaluate(entry.getValue(), bound));
    }

    //chain to embedded page (widget), with arguments
    EmbeddedRespond embed = factory.get(arguments);

    Request req = request.get();
    try {
      page.doMethod(req.method(), pageObject, "", req);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    page.widget().render(pageObject, embed);

    //extract and write embedded response to enclosing page's respond
    respond.writeToHead(embed.toHeadString()); //TODO only write @Require tags
    respond.write(embed.toString());

    embed.clear();
  }

  public <T extends Renderable> Set<T> collect(Class<T> clazz) {
    return Collections.emptySet();
  }

  @Inject
  public void init(EmbeddedRespondFactory factory, Provider<Request> request) {
    this.factory = factory;
    this.request = request;
  }
}
