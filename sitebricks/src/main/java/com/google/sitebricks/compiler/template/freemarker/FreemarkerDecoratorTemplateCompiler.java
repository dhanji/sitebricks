package com.google.sitebricks.compiler.template.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.sitebricks.decorator.Decorator;
import org.sitebricks.decorator.freemarker.FreemarkerDecorator;
import org.sitebricks.extractor.ExtractResult;
import org.sitebricks.extractor.XhtmlExtractor;
import org.sitebricks.extractor.jsoup.JSoupXhtmlExtractor;

import com.google.common.collect.ImmutableSet;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

/**
 * Creates renderables, given a Freemarker decorator template and simple xhtml content that is parsed by JSoup. We take the title, head, body, and links as parsed from JSoup and inject them into the
 * template context as ${title}, ${head}, ${body}, and ${links}.
 */
public class FreemarkerDecoratorTemplateCompiler {
  private final Class<?> page;
  private final Decorator decorator;
  private final XhtmlExtractor extractor;

  public FreemarkerDecoratorTemplateCompiler(Class<?> page) {
    this.page = page;
    this.decorator = new FreemarkerDecorator();
    this.extractor = new JSoupXhtmlExtractor();
  }

  // We want to be able to use the decorator and inject bits into it using freemarker
  // We want the individual page to still be able to use freemarker to inject bits
  // For a purely javascript based application the decorator being able to do this is probably the only thing that really matters, but for page based apps it's important
  // for the individual pages to be injectable.
  
  public Renderable compile(final String pageContent) {
            
    return new Renderable() {
      @Override
      public void render(Object bound, Respond respond) {
        assert page.isInstance(bound);
        //
        // Process the page with Freemarker
        //
        Template template = getTemplate(page, pageContent);
        Writer pageWriter = new StringWriter();
        try {
          template.process(bound, pageWriter);
        } catch (TemplateException e) {
          throw new RuntimeException(e);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        //
        // Extract content from page
        //
        ExtractResult er = extractor.extract(pageWriter.toString());
        Map<String,Object> context = new HashMap<String,Object>();
        context.put("title", er.getTitle());
        context.put("body", er.getBody());
        context.put("head", er.getHead());
        context.put("links", er.getLinks());
        Writer writer = new StringWriter();

        //
        // Find the proper decorator based on the templates location in the tree of pages
        //
        File decoratorSource = new File("/Users/jvanzyl/js/insight-bootstrap/src/main/webapp/template.html");
        decorator.decorate(decoratorSource, context, writer);
        respond.write(writer.toString());
      }

      @Override
      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return ImmutableSet.of();
      }
    };
  }

  private Template getDecoratorTemplate(Class<?> page, String pageContent) {
    Configuration configuration = new Configuration();
    configuration.setTemplateExceptionHandler(new SitebricksTemplateExceptionHandler());

    try {
      return new Template(page.getName(), new StringReader(pageContent), configuration);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
