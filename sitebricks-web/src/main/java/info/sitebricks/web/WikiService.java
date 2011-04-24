package info.sitebricks.web;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;
import com.google.sitebricks.rendering.Templates;
import com.petebevin.markdown.MarkdownProcessor;
import info.sitebricks.data.Document;
import info.sitebricks.persist.WikiStore;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/ajax") @Service
public class WikiService {
  public static final String TEXT_HTML_CHARSET_UTF8 = "text/html; charset=utf8";
  private final WikiStore wikiStore;
  private final MarkdownProcessor markdownProcessor;
  private final Templates templates;

  @Inject
  public WikiService(WikiStore wikiStore, MarkdownProcessor markdownProcessor,
                     Templates templates) {
    this.wikiStore = wikiStore;
    this.markdownProcessor = markdownProcessor;
    this.templates = templates;
  }

  @At("/page/:name") @Post
  Reply<?> renderedPage(@Named("name") String name) {
    // Look up page by name, then render it with markdownj.
    Document document = wikiStore.fetch(name);
    document.setTemporaryMarkdownProcessor(markdownProcessor);

    return Reply.with(templates.render(Document.class, document))
        .type(TEXT_HTML_CHARSET_UTF8);
  }

  @At("/markdown/:name") @Post
  Reply<?> markdown(@Named("name") String name) {
    // Look up page by name, then return the raw markdown.
    Document document = wikiStore.fetch(name);

    return Reply.with(document.getText())
        .type(TEXT_HTML_CHARSET_UTF8);
  }
}
