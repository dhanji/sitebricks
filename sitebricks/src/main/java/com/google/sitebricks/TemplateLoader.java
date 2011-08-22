package com.google.sitebricks;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.compiler.Compilers;
import net.jcip.annotations.Immutable;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
public class TemplateLoader {
  private final Provider<ServletContext> context;
    private Compilers compilers;

    @Inject
  public TemplateLoader(Compilers compilers, Provider<ServletContext> context) {
    this.compilers = compilers;
    this.context = context;
  }

  public Template load(Class<?> pageClass) {
	  // try to find the template name
    Show show = pageClass.getAnnotation(Show.class);
    String template = null;
    if (null != show) {
      template = show.value();
    }
    
    // an empty string means no template name was given
    if (template == null || template.length() == 0) {
      // use the default name for the page class
      template = resolve(pageClass);
    }

    String text;
    try {
      InputStream stream = null;
      //first look in class neighborhood for template
      if (null != template) {
        stream = pageClass.getResourceAsStream(template);
      }

      //look on the webapp resource path if not in classpath
      if (null == stream) {

        final ServletContext servletContext = context.get();
        if (null != template)
          stream = open(template, servletContext);

        //resolve again, but this time on the webapp resource path
        if (null == stream) {
          final ResolvedTemplate resolvedTemplate = resolve(pageClass, servletContext, template);
          if (null != resolvedTemplate) {
            template = resolvedTemplate.templateName;
            stream = resolvedTemplate.resource;
          }
        }

        //if there's still no template, then error out
        if (null == stream) {
          List<String> templateNames = new ArrayList<String>(compilers.getRegisteredExtensions().size());
            for (String fileNameTemplate : compilers.getRegisteredExtensions()) {
                templateNames.add(String.format(fileNameTemplate, pageClass.getSimpleName()));
            }

          throw new MissingTemplateException(String.format("Could not find a suitable template for %s. " +
              "did you remember to place an @Show?\n" +
              "None of %s could be found in package [%s], OR in the root of the resource dir OR in WEB-INF/.",
              pageClass.getName(), templateNames,
              pageClass.getPackage().getName()));
        }
      }

      text = read(stream);
    } catch (IOException e) {
      throw new TemplateLoadingException("Could not load template for (i/o error): " + pageClass, e);
    }

    return new Template(template, text);
  }

  private ResolvedTemplate resolve(Class<?> pageClass, ServletContext context, String template) {
    //first resolve using url conversion
    for (String extension : compilers.getRegisteredExtensions()) {
      String nameTemplate = "%s." + extension;
      String templateName = String.format(nameTemplate, pageClass.getSimpleName());
      InputStream resource = open(templateName, context);

      if (null != resource) {
        return new ResolvedTemplate(templateName, resource);
      }

      resource = openWebInf(templateName, context);

      if (null != resource) {
        return new ResolvedTemplate(templateName, resource);
      }


      if (null == template) {
          continue;
      }
      //try to resolve @Show template from web-inf folder    
      resource = openWebInf(template, context);

      if (null != resource) {
          return new ResolvedTemplate(template, resource);
      }
    }

    //resolve again using servlet context if that fails
    for (String nameTemplate : compilers.getRegisteredExtensions()) {
      String templateName = String.format("%s." + nameTemplate, pageClass.getSimpleName());
      InputStream resource = context.getResourceAsStream(templateName);

      if (null != resource) {
        return new ResolvedTemplate(templateName, resource);
      }
    }

    return null;
  }

  private static class ResolvedTemplate {
    private final InputStream resource;
    private final String templateName;

    private ResolvedTemplate(String templateName, InputStream resource) {
      this.templateName = templateName;
      this.resource = resource;
    }
  }

  private static InputStream open(String file, ServletContext context) {
    try {
      String path = context.getRealPath(file);
      return path == null ? null : new FileInputStream(new File(path));
    } catch (FileNotFoundException e) {
      return null;
    }
  }
  
  private static InputStream openWebInf(String file, ServletContext context) {
    return open("/WEB-INF/" + file, context);
  }

  //resolves a location for this page class's template (assuming @Show is not present)
  private String resolve(Class<?> pageClass) {
    for (String nameTemplate : compilers.getRegisteredExtensions()) {
      String name = String.format("%s." + nameTemplate, pageClass.getSimpleName());
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
}
