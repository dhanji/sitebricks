package com.google.sitebricks.compiler.template.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.sitebricks.decorator.Decorator;
import org.sitebricks.decorator.freemarker.FreemarkerDecorator;

import com.google.sitebricks.compiler.TemplateRenderer;
import com.google.sitebricks.compiler.template.AbstractMagicTemplateCompiler;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Creates renderables, given a Freemarker decorator template and simple xhtml content that is parsed by JSoup. We take the title, head, body, and links as parsed from JSoup and inject them into the
 * template context as ${title}, ${head}, ${body}, and ${links}.
 */
public class FreemarkerDecoratorTemplateCompiler extends AbstractMagicTemplateCompiler implements TemplateRenderer {
  
  private final Decorator decorator;

  public FreemarkerDecoratorTemplateCompiler(Class<?> page) {
    super(page);
    this.decorator = new FreemarkerDecorator();
  }
  
  @Override
  public com.google.sitebricks.Template transform(com.google.sitebricks.Template template) {
    //
    // Whatever, change markdown to XHTML
    //
    return template;
  }

  @Override
  public String process(Object bound, com.google.sitebricks.Template sitebricksTemplate) {
    //
    // Process the page with Freemarker
    //
    Template template = getTemplate(page, sitebricksTemplate.getText());
    Writer pageWriter = new StringWriter();
    try {
      template.process(bound, pageWriter);
    } catch (TemplateException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    Map<String,Object> context = new HashMap<String,Object>();
    Writer writer = new StringWriter();

    //
    // Find the proper decorator based on the templates location in the tree of pages. I think a standard name for the decorator is
    // fine, but we need to account for the case where a pages in the same level of the hierarchy need to use different decorators.
    // Need to account for the case where there are individual pages that have unique decorators and groups of pages that have a
    // specific decorator.
    //
    File decoratorSource = new File(new File(sitebricksTemplate.getTemplateSource().getLocation()).getParentFile(), "template.html");
    decorator.decorate(decoratorSource, pageWriter.toString(), context, writer);
    return writer.toString();
  }

  private Template getTemplate(Class<?> page, String pageContent) {
    Configuration configuration = new Configuration();
    configuration.setTemplateExceptionHandler(new SitebricksTemplateExceptionHandler());

    try {
      return new Template(page.getName(), new StringReader(pageContent), configuration);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  class SitebricksTemplateExceptionHandler implements TemplateExceptionHandler {
    public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
      // We intentionally do nothing here
    }
  }    
}
