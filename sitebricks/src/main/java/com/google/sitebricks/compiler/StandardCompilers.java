package com.google.sitebricks.compiler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.Bricks;
import com.google.sitebricks.MissingTemplateException;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Show;
import com.google.sitebricks.TemplateLoader;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.rendering.Decorated;
import com.google.sitebricks.routing.PageBook;

/**
 * A factory for internal template compilers.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@Singleton
class StandardCompilers implements Compilers {
  private final PageBook pageBook;
  private final Map<String, Class<? extends Annotation>> httpMethods;
  private final TemplateLoader loader;

  @Inject
  public StandardCompilers(PageBook pageBook, @Bricks Map<String, Class<? extends Annotation>> httpMethods, TemplateLoader loader) {
    this.pageBook = pageBook;
    this.httpMethods = httpMethods;
    this.loader = loader;
  }
  
  // TODO(dhanji): Feedback errors as return rather than throwing.
  public void analyze(Class<?> page) {
    //
    // May move this into a separate class if it starts getting too big.
    //
    analyzeMethods(page.getDeclaredMethods());
    analyzeMethods(page.getMethods());
  }

  private void analyzeMethods(Method[] methods) {
    for (Method method : methods) {
      for (Annotation annotation : method.getDeclaredAnnotations()) {
        //
        // if this is a http method annotation, do some checking on the args and return types.
        //
        if (httpMethods.containsValue(annotation.annotationType())) {
          Class<?> returnType = method.getReturnType();

          PageBook.Page page = pageBook.forClass(returnType);
          if (null == page) {
            //
            // throw an error.
            //
          } else {
            //
            // do further analysis on this sucka
            //
            if (page.getUri().contains(":")) {
              //
              // throw an error coz we cant redir to dynamic URLs
              //
            }
            //
            // If this is headless, it MUST return an instance of reply.
            //
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
  
  public void compilePage(PageBook.Page page) {
    //
    // find the template page class
    //
    Class<?> templateClass = page.pageClass();
    //
    // root page uses the last template, extension uses its own embedded template
    //
    if (!page.isDecorated() && templateClass.isAnnotationPresent(Decorated.class)) {
      //
      // the first superclass with a @Show and no @Extension is the template
      //
      while (!templateClass.isAnnotationPresent(Show.class) || templateClass.isAnnotationPresent(Decorated.class)) {
        templateClass = templateClass.getSuperclass();
        if (templateClass == Object.class) {
          throw new MissingTemplateException("Could not find tempate for " + page.pageClass() + ". You must use @Show on a superclass of an @Extension page");
        }
      }
    }

//    Renderable widget = compile(page.pageClass());
    Renderable widget = loader.compile(page);

    //
    //apply the compiled widget chain to the page (completing compile step)
    //
    page.apply(widget);
  }
    
  @Override
  public Renderable compile(Class<?> templateClass) {    
    return loader.compile(templateClass);
  }  
}
