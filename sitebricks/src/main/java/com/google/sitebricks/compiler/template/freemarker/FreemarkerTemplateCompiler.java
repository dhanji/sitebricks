package com.google.sitebricks.compiler.template.freemarker;

import com.google.common.collect.ImmutableSet;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.compiler.TemplateCompiler;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

/**
 * Creates renderables, given a Freemarker template page.
 */
public class FreemarkerTemplateCompiler implements TemplateCompiler {
  private final Class<?> page;
    private ServletContext servletContext;

    public FreemarkerTemplateCompiler(Class<?> page, ServletContext servletContext) {
    this.page = page;
        this.servletContext = servletContext;
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
    configuration.setServletContextForTemplateLoading(servletContext, "/");
      
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
