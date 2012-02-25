package com.google.sitebricks.compiler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.Bricks;
import com.google.sitebricks.MissingTemplateException;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Show;
import com.google.sitebricks.Template;
import com.google.sitebricks.TemplateLoader;
import com.google.sitebricks.compiler.template.MvelTemplateCompiler;
import com.google.sitebricks.compiler.template.freemarker.FreemarkerDecoratorTemplateCompiler;
import com.google.sitebricks.compiler.template.freemarker.FreemarkerTemplateCompiler;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.rendering.Decorated;
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
  private final TemplateLoader loader;

  @Inject
  public StandardCompilers(WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics,
                           @Bricks Map<String, Class<? extends Annotation>> httpMethods, TemplateLoader loader) {
    this.registry = registry;
    this.pageBook = pageBook;
    this.metrics = metrics;
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

    Renderable widget = compile(templateClass);

    //
    //apply the compiled widget chain to the page (completing compile step)
    //
    page.apply(widget);
  }
  
  @Override
  public Renderable compile(Class<?> templateClass) {
    final Template template = loader.load(templateClass);

    Renderable widget;

    switch(template.getKind()) {
      default:
      case HTML:
        widget = new HtmlTemplateCompiler(templateClass, new MvelEvaluatorCompiler(templateClass), registry, pageBook, metrics).compile(template.getText()); 
        //compileHtml(templateClass, template.getText());
        break;
      case XML:
        widget = new XmlTemplateCompiler(templateClass, new MvelEvaluatorCompiler(templateClass), registry, pageBook, metrics).compile(template.getText());         
        //compileXml(templateClass, template.getText());
        break;
      case FLAT:
        widget = new FlatTemplateCompiler(templateClass, new MvelEvaluatorCompiler(templateClass), metrics, registry).compile(template.getText()); 
        //compileFlat(templateClass, template.getText());
        break;
      case MVEL:
        /**
         * Creates a Renderable that can process MVEL templates.
         * These are not to be confused with Sitebricks templates
         * that *use* MVEL. Rather, this is MVEL's template technology.
         */                
        widget = new MvelTemplateCompiler(templateClass).compile(template.getText()); 
        //compileMvel(templateClass, template.getText());
        break;
      case FREEMARKER:
        widget = new FreemarkerTemplateCompiler(templateClass).compile(template); 
        //compileFreemarker(templateClass, template);
        break;
      case MAGIC:
        widget = new FreemarkerDecoratorTemplateCompiler(templateClass).compile(template); 
        //compileMagicTemplate(templateClass, template);
        break;
    }
    return widget;
  }

}
