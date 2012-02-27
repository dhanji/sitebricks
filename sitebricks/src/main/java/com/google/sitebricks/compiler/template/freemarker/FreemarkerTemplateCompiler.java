package com.google.sitebricks.compiler.template.freemarker;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import com.google.inject.Singleton;
import com.google.sitebricks.compiler.TemplateCompiler;
import com.google.sitebricks.compiler.template.AbstractMagicTemplateCompiler;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@Singleton
public class FreemarkerTemplateCompiler extends AbstractMagicTemplateCompiler implements TemplateCompiler {

  @Override
  public String process(Class<?> page, Object bound, com.google.sitebricks.Template sitebricksTemplate) {

    final Template template = getTemplate(page, sitebricksTemplate);

    // pick type
    // transform to xhtml
    // produce output

    Writer writer = new StringWriter();
    try {
      template.process(bound, writer);
    } catch (TemplateException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return writer.toString();
  }

  private Template getTemplate(Class<?> page, com.google.sitebricks.Template sitebricksTemplate) {
    Configuration configuration = new Configuration();
    configuration.setTemplateExceptionHandler(new SitebricksTemplateExceptionHandler());

    try {
      return new Template(page.getName(), new StringReader(sitebricksTemplate.getText()), configuration);
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
