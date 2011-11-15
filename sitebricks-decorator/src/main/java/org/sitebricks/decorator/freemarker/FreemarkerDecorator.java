package org.sitebricks.decorator.freemarker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sitebricks.decorator.Decorator;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@Named
@Singleton
public class FreemarkerDecorator implements Decorator {

  private Configuration configuration;

  public FreemarkerDecorator() {
    configuration = new Configuration();
    configuration.setTemplateExceptionHandler(new IdiomTemplateExceptionHandler());
  }

  public void decorate(String decorator, Map<String, Object> context, Writer writer) {
    decorate(new StringReader(decorator), "", context, writer);    
  }

  public void decorate(File decorator, Map<String, Object> context, Writer writer) {
    try {
      decorate(new FileReader(decorator), "", context, writer);
    } catch (FileNotFoundException e) {
      throw new RuntimeException("Error processing template: ", e);
    }    
  }

  public void decorate(Reader decoratorSource, String templateName, Map<String, Object> context, Writer writer) {
    try {
      Template template = new Template(templateName, decoratorSource, configuration);
      template.process(context, writer);
    } catch (TemplateException e) {
      throw new RuntimeException("Error processing template: ", e);
    } catch (IOException e) {
      throw new RuntimeException("Error processing template: ", e);
    }
  }

  public static class CannotCreateSkinException extends RuntimeException {
    public CannotCreateSkinException(Throwable throwable) {
      super(throwable);
    }
  }

  public static class CannotApplySkinException extends RuntimeException {
    public CannotApplySkinException(Throwable throwable) {
      super(throwable);
    }
  }

  class IdiomTemplateExceptionHandler implements TemplateExceptionHandler {
    public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
    }
  }
}
