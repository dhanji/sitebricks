package com.google.sitebricks.compiler.template.freemarker;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Creates renderables, given a Freemarker template page.
 */
public class FreemarkerTemplateCompiler {
  private final Class<?> page;

  public FreemarkerTemplateCompiler(Class<?> page) {
    this.page = page;
  }

  public Renderable compile(String templateContent) {
          
    final Template template = getTemplate(page, templateContent);    

    return new Renderable() {
      @Override
      public void render(Object bound, Respond respond) {
        assert page.isInstance(bound);
        Writer writer = new StringWriter();
        try {
            template.process(bound, writer);
        }
        catch (TemplateException e) {
            throw new RuntimeException(e);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }          
        respond.write(writer.toString());
      }

      @Override
      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return ImmutableSet.of();
      }
    };
  }
  
  private Template getTemplate(Class<?> page, String content)
  {
    Configuration configuration = new Configuration();
    configuration.setTemplateExceptionHandler( new SitebricksTemplateExceptionHandler() );
      
    try {
      return new Template(page.getName(), new StringReader(content), configuration);
    }
    catch ( IOException e ) {
      throw new RuntimeException( e );
    }          
  }
  
  class SitebricksTemplateExceptionHandler implements TemplateExceptionHandler {
    public void handleTemplateException(TemplateException te, Environment env, Writer out) 
      throws TemplateException {        
      // We intentionally do nothing here
    }
  }
}
