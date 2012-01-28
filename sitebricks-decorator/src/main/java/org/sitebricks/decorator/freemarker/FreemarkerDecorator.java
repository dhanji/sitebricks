package org.sitebricks.decorator.freemarker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jsoup.Jsoup;
import org.sitebricks.decorator.Decorator;
import org.sitebricks.extractor.ExtractResult;
import org.sitebricks.extractor.jsoup.JSoupXhtmlExtractor;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@Named
@Singleton
public class FreemarkerDecorator implements Decorator {

  private Configuration configuration;
  private JSoupXhtmlExtractor extractor;

  public FreemarkerDecorator() {
    configuration = new Configuration();
    configuration.setTemplateExceptionHandler(new IdiomTemplateExceptionHandler());
    extractor = new JSoupXhtmlExtractor();
    
  }

  public void decorate(File decorator, String xhtml, Map<String, Object> context, Writer writer) {
    //
    // Extract content from page
    //
    ExtractResult er = extractor.extract(xhtml);
    context.put("title", er.getTitle());
    context.put("body", er.getBody());
    context.put("head", er.getHead());
    context.put("links", er.getLinks());
    
    decorate(decorator, context, writer);
  }
  
  
  public void decorate(File decorator, Map<String, Object> context, Writer writer) {

    //
    // The decorator may have associated with it some "bricks" that we want to read, parse, and
    // place into the context. For example we may want to share a chunk of markup that represents
    // the navigation for a shared set of pages.
    //

    File brickSource = new File(decorator.getParentFile(), "bricks");

    if (brickSource.exists()) {
      try {
        List<String> files = FileUtils.getFileNames(brickSource, "*-brick.html", null, false);
        for (String fileName : files) {
          File brickFile = new File(brickSource, fileName);
          String brickId = brickFile.getName().substring(0, brickFile.getName().indexOf("-brick"));
          
          ExtractResult result = extractor.extract(brickFile);
          
          if(result.getBody() != null && result.getBody().trim().length() != 0) {
            context.put(brickId, result.getBody());            
          } else if (result.getHead() != null && result.getHead().trim().length() != 0) {
            context.put(brickId, result.getHead());
          }          
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    Reader decoratorSource = null;
    try {
      decoratorSource = new FileReader(decorator);
      Template template = new Template(decorator.getName(), decoratorSource, configuration);
      template.process(context, writer);
    } catch (TemplateException e) {
      throw new RuntimeException("Error processing template: ", e);
    } catch (IOException e) {
      throw new RuntimeException("Error processing template: ", e);
    } finally {
      IOUtil.close(decoratorSource);
    }
  }

  public static class CannotCreateSkinException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CannotCreateSkinException(Throwable throwable) {
      super(throwable);
    }
  }

  public static class CannotApplySkinException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CannotApplySkinException(Throwable throwable) {
      super(throwable);
    }
  }

  class IdiomTemplateExceptionHandler implements TemplateExceptionHandler {
    public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
    }
  }
}
