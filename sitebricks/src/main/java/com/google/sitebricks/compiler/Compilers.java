package com.google.sitebricks.compiler;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.routing.PageBook;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ImplementedBy(StandardCompilers.class)
public interface Compilers {
  
  /**
   * Performs static analysis of the given page class to determine some types of errors.
   */
  void analyze(Class<?> page);
  
  void compilePage(PageBook.Page page);

  Renderable compile(Class<?> templateClass);
  
}
