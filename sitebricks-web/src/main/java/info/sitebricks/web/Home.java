package info.sitebricks.web;

import com.google.inject.Inject;
import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.rendering.Templates;
import com.petebevin.markdown.MarkdownProcessor;
import info.sitebricks.data.Document;
import info.sitebricks.data.Index;
import info.sitebricks.persist.WikiStore;

import javax.servlet.http.HttpServletResponse;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/")
public class Home {
  private final WikiStore store;
  private final MarkdownProcessor markdownProcessor;
  private final Templates templates;

  @Inject
  public Home(WikiStore store, MarkdownProcessor markdownProcessor, Templates templates) {
    this.store = store;
    this.markdownProcessor = markdownProcessor;
    this.templates = templates;
  }

  private Document home;
  private Index index;

  @Get
  public void home(HttpServletResponse response) {
    response.setContentType(WikiService.TEXT_HTML_CHARSET_UTF8);

    index = store.fetchIndex();  // ensures "Home" exists
    home = store.fetch("home");
  }

  public String renderHome() {
    home.setTemporaryMarkdownProcessor(markdownProcessor);

    return templates.render(Document.class, home);
  }

  public Index getIndex() {
    return index;
  }
}
