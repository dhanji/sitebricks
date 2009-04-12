package com.google.sitebricks.compiler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

/**
 * A factory for internal template compilers.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@Singleton
class CompilersImpl implements Compilers {
  private final WidgetRegistry registry;
  private final PageBook pageBook;
  private final SystemMetrics metrics;

  @Inject
  public CompilersImpl(WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics) {
    this.registry = registry;
    this.pageBook = pageBook;
    this.metrics = metrics;
  }

  public Renderable compileXml(Class<?> page, String template) {
    return new XmlTemplateCompiler(page, new MvelEvaluatorCompiler(page), registry, pageBook,
        metrics)
        .compile(template);
  }


  public Renderable compileFlat(Class<?> page, String template) {
    return new FlatTemplateCompiler(page, new MvelEvaluatorCompiler(page), metrics, registry)
        .compile(template);
  }
}
