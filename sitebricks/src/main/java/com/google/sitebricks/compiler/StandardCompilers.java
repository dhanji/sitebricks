package com.google.sitebricks.compiler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.Bricks;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * A factory for internal template compilers.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@Singleton
class StandardCompilers implements Compilers {
  private final WidgetRegistry registry;
  private final PageBook pageBook;
  private final SystemMetrics metrics;
  private final Map<String, Class<? extends Annotation>> httpMethods;

  @Inject
  public StandardCompilers(WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics,
                           @Bricks Map<String, Class<? extends Annotation>> httpMethods) {
    this.registry = registry;
    this.pageBook = pageBook;
    this.metrics = metrics;
    this.httpMethods = httpMethods;
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


  // TODO(dhanji): Feedback errors as return rather than throwing.
  public void analyze(Class<?> page) {
    // May move this into a separate class if it starts getting too big.
    analyzeMethods(page.getDeclaredMethods());
    analyzeMethods(page.getMethods());
  }

  private void analyzeMethods(Method[] methods) {
    for (Method method : methods) {
      for (Annotation annotation : method.getDeclaredAnnotations()) {
        // if this is a http method annotation, do some checking on the
        // args and return types.
        if (httpMethods.containsValue(annotation.annotationType())) {
          Class<?> returnType = method.getReturnType();

          PageBook.Page page = pageBook.forClass(returnType);
          if (null == page) {
            // throw an error.
          } else {
            // do further analysis on this sucka
            if (page.getUri().contains(":"))
              ; // throw an error coz we cant redir to dynamic URLs


            // If this is headless, it MUST return an instance of reply.
            if (page.isHeadless()) {
              if (!Reply.class.isAssignableFrom(method.getReturnType())) {
                // throw error
              }
            }
          }
        }
      }
    }
  }
}
