package com.google.sitebricks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.ServletContext;

import net.jcip.annotations.Immutable;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.compiler.TemplateCompiler;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
public class TemplateLoader {
  
  private final Provider<ServletContext> context;
  private final TemplateSystem templateSystem;
  
  @Inject
  public TemplateLoader(WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics, Provider<ServletContext> context, TemplateSystem templateSystem) {
    this.context = context;
    this.templateSystem = templateSystem;
  }

  public Template load(Class<?> pageClass) {
    //
	  // try to find the template name
    //
    Show show = pageClass.getAnnotation(Show.class);
    String template = null;
    if (null != show) {
      template = show.value();
    }
    
    //
    // an empty string means no template name was given
    //
    if (template == null || template.length() == 0) {
      // use the default name for the page class
      template = resolve(pageClass);
    }

    TemplateSource templateSource = null;
    
    String text;
    try {
      InputStream stream = null;
      //      
      //first look in class neighborhood for template
      //
      if (null != template) {
        stream = pageClass.getResourceAsStream(template);
      }

      //
      //look on the webapp resource path if not in classpath
      //
      if (null == stream) {

        final ServletContext servletContext = context.get();
        if (null != template) {
          stream = open(template, servletContext).resource;
        }

        //
        //resolve again, but this time on the webapp resource path
        //
        if (null == stream) {
          final ResolvedTemplate resolvedTemplate = resolve(pageClass, servletContext, template);
          if (null != resolvedTemplate) {
            template = resolvedTemplate.templateName;
            stream = resolvedTemplate.resource;
            templateSource = new FileTemplateSource(resolvedTemplate.templateFile);
          }
        }

        //
        //if there's still no template, then error out
        //
        if (null == stream) {
          throw new MissingTemplateException(String.format("Could not find a suitable template for %s, " + "did you remember to place an @Show? None of [" +
              templateSystem.getTemplateExtensions()[0] + "] could be found in either package [%s], in the root of the resource dir OR in WEB-INF/.",
              pageClass.getName(), pageClass.getSimpleName(),
              pageClass.getPackage().getName()));
        }
      }

      text = read(stream);
    } catch (IOException e) {
      throw new TemplateLoadingException("Could not load template for (i/o error): " + pageClass, e);
    }

    return new Template(template, text, templateSource);
  }
  
  private ResolvedTemplate resolve(Class<?> pageClass, ServletContext context, String template) {
    //first resolve using url conversion
    for (String nameTemplate : templateSystem.getTemplateExtensions()) {
      String templateName = String.format(nameTemplate, pageClass.getSimpleName());
      ResolvedTemplate resolvedTemplate = open(templateName, context);

      if (null != resolvedTemplate.resource) {
        return resolvedTemplate;
      }

      resolvedTemplate = openWebInf(templateName, context);

      if (null != resolvedTemplate.resource) {
        return resolvedTemplate;
      }

      if (null == template) {
          continue;
      }
      //try to resolve @Show template from web-inf folder  
      resolvedTemplate = openWebInf(template, context);

      if (null != resolvedTemplate.resource) {
          return resolvedTemplate;
      }
    }

    //resolve again using servlet context if that fails
    for (String nameTemplate : templateSystem.getTemplateExtensions()) {
      String templateName = String.format(nameTemplate, pageClass.getSimpleName());
      InputStream resource = context.getResourceAsStream(templateName);

      if (null != resource) {
        return new ResolvedTemplate(templateName, resource, null);
      }
    }

    return null;
  }

  private static class ResolvedTemplate {
    private final InputStream resource;
    private final String templateName;
    private final File templateFile;

    private ResolvedTemplate(String templateName, InputStream resource, File templateFile) {
      this.templateName = templateName;
      this.resource = resource;
      this.templateFile = templateFile;
    }
  }

  private static ResolvedTemplate open(String templateName, ServletContext context) {
    try {      
      String path = context.getRealPath(templateName);
      return path == null ? null : new ResolvedTemplate(templateName, new FileInputStream(path), new File(path));
    } catch (FileNotFoundException e) {
      return new ResolvedTemplate(templateName, null, null);
    }
  }
  
  private static ResolvedTemplate openWebInf(String templateName, ServletContext context) {
    return open("/WEB-INF/" + templateName, context);
  }

  //resolves a location for this page class's template (assuming @Show is not present)
  private String resolve(Class<?> pageClass) {
    for (String nameTemplate : templateSystem.getTemplateExtensions()) {
      String name = String.format(nameTemplate, pageClass.getSimpleName());
      URL resource = pageClass.getResource(name);

      if (null != resource) {
        return name;
      }
    }

    return null;
  }

  private static String read(InputStream stream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

    StringBuilder builder = new StringBuilder();
    try {
      while (reader.ready()) {
        builder.append(reader.readLine());
        builder.append("\n");
      }
    } finally {
      stream.close();
    }

    return builder.toString();
  }

  public Renderable compile(Class<?> templateClass) {   
    Template template = load(templateClass);
    TemplateCompiler templateCompiler = templateSystem.compilerFor(template.getName());
    //
    // This is how the old mechanism worked, for example if dynamic.js comes through the system we still pass back
    // the html compiler. JVZ: not sure why this wouldn't be directly routed to the right resource. TODO: investigate
    //
    if(templateCompiler == null) {
      templateCompiler = templateSystem.compilerFor("html");
    }
        
    return templateCompiler.compile(templateClass, template);
  }  
}
