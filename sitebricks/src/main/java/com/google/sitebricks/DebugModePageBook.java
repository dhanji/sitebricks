package com.google.sitebricks;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.servlet.RequestScoped;
import com.google.sitebricks.compiler.Compilers;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.Production;
import com.google.sitebricks.routing.SystemMetrics;
import net.jcip.annotations.ThreadSafe;

import java.util.HashSet;
import java.util.Set;

/**
 * Used in the development stage to intercept the real pagebook so we can reload
 * & recompile templates on demand.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ThreadSafe
@Singleton
class DebugModePageBook implements PageBook {
  private final PageBook book;
  private final Provider<TemplateLoader> templateLoader;
  private final SystemMetrics metrics;
  private final Compilers compilers;
  private final Provider<Memo> memo;

  @Inject
  public DebugModePageBook(@Production PageBook book,
                           Provider<TemplateLoader> templateLoader,
                           SystemMetrics metrics, Compilers compilers,
                           Provider<Memo> memo) {

    this.book = book;
    this.templateLoader = templateLoader;
    this.metrics = metrics;
    this.compilers = compilers;
    this.memo = memo;
  }

  public Page at(String uri, Class<?> myPageClass) {
    return book.at(uri, myPageClass);
  }

  public Page get(String uri) {
    final Page page = book.get(uri);

    //reload template
    reload(uri, page);

    return page;
  }

  public Page forName(String name) {
    final Page page = book.forName(name);

    //reload template
    reload(name, page);

    return page;
  }

  public Page embedAs(Class<?> page, String as) {
    return book.embedAs(page, as);
  }

  public Page nonCompilingGet(String uri) {
    // Simply delegate thru to the real page book.
    return book.get(uri);
  }

  public Page forInstance(Object instance) {
    return book.forInstance(instance);
  }

  public Page forClass(Class<?> pageClass) {
    return book.forClass(pageClass);
  }

  public Page serviceAt(String uri, Class<?> pageClass) {
    return book.serviceAt(uri, pageClass);
  }

  private void reload(String identifier, Page page) {

    // Do nothing on the first pass since the page is already compiled.
    // Also skips static resources and headless web services.
    if (null == page || !metrics.isActive() || page.isHeadless())
      return;

    // Ensure we reload only once per request, per identifier.
    final Memo memo = this.memo.get();
    if (memo.uris.contains(identifier))
      return;

    // Otherwise, remember that we already loaded it in this request.
    memo.uris.add(identifier);


    final Class<?> pageClass = page.pageClass();
    final Template template = templateLoader.get().load(pageClass);

    // TODO(dhanji): Merge this with the duplicated code in ScanAndCompileBootstrapper
    switch (template.getKind()) {
      case HTML:
        page.apply(compilers.compileHtml(pageClass, template.getText()));
        break;
      case XML:
        page.apply(compilers.compileXml(pageClass, template.getText()));
        break;
      case FLAT:
        page.apply(compilers.compileFlat(pageClass, template.getText()));
        break;
      case MVEL:
        page.apply(compilers.compileMvel(pageClass, template.getText()));
        break;
      case FREEMARKER:
        page.apply(compilers.compileFreemarker(pageClass, template.getText())); 
        break;        
    }
  }

  @RequestScoped
  private static class Memo {
    private final Set<String> uris = new HashSet<String>();
  }
}
