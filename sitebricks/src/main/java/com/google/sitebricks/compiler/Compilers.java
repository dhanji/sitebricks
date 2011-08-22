package com.google.sitebricks.compiler;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.routing.PageBook;

import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ImplementedBy(PluggableCompilers.class)
public interface Compilers {

  public void register(CompilerFactory compilerFactory, String ... extension);

  public Set<String> getRegisteredExtensions();

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
