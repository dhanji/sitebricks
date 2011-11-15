package com.google.sitebricks.compiler;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.routing.PageBook;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ImplementedBy(StandardCompilers.class)
public interface Compilers {
  Renderable compileHtml(Class<?> page, String template);

  Renderable compileXml(Class<?> page, String template);

  Renderable compileFlat(Class<?> page, String template);

  /**
   * Creates a Renderable that can process MVEL templates.
   * These are not to be confused with Sitebricks templates
   * that *use* MVEL. Rather, this is MVEL's template technology.
   */
  Renderable compileMvel(Class<?> page, String template);

  Renderable compileFreemarker( Class<?> page, String text );
  Renderable compileFreemarkerDecorator( Class<?> page, String text );

  /**
   * Performs static analysis of the given page class to
   * determine some types of errors.
   */
  void analyze(Class<?> page);
  

  void compilePage(PageBook.Page page);

  /**
   * Convenience method, use this instead of compileXXX to hide
   * the underlying template type if you dont care what it is.
   */
  Renderable compile(Class<?> templateClass);
}
