package com.google.sitebricks;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.jcip.annotations.Immutable;

import javax.servlet.ServletContext;
import java.io.*;
import java.net.URL;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
class TemplateLoader {
  private final Provider<ServletContext> context;

  private final String[] fileNameTemplates = new String[] { "%s.html", "%s.xhtml", "%s.xml" };

  @Inject
  public TemplateLoader(Provider<ServletContext> context) {
    this.context = context;
  }

  public Template load(Class<?> pageClass) {
    Show show = pageClass.getAnnotation(Show.class);
    String template;

    //annotation not present, resolve by name
    if (null == show) {
      template = resolve(pageClass);
    } else {
      template = show.value();
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
          final ResolvedTemplate resolvedTemplate = resolve(pageClass, servletContext);
          if (null != resolvedTemplate) {
            template = resolvedTemplate.templateName;
            stream = resolvedTemplate.resource;
          }
        }

        //if there's still no template, then error out
        if (null == stream)
          throw new MissingTemplateException(String.format("Could not find a suitable template for %s, did you remember to place " +
              "an @Show? None of [%s.html, %s.xhtml or %s.xml] could be found in either " +
              "package [%s] OR in the root of the resource dir.", pageClass.getName(), pageClass.getSimpleName(),
              pageClass.getSimpleName(), pageClass.getSimpleName(), pageClass.getPackage().getName()));
      }

      text = read(stream);
    } catch (IOException e) {
      throw new TemplateLoadingException("Could not load template for (i/o error): " + pageClass, e);
    }

    return new Template(Template.Kind.kindOf(template), text);
  }

  private ResolvedTemplate resolve(Class<?> pageClass, ServletContext context) {
    //first resolve using url conversion
    for (String nameTemplate : fileNameTemplates) {
      String templateName = String.format(nameTemplate, pageClass.getSimpleName());
      InputStream resource = open(templateName, context);

      if (null != resource) {
        return new ResolvedTemplate(templateName, resource);
      }

      resource = openWebInf(templateName, context);

      if (null != resource) {
        return new ResolvedTemplate(templateName, resource);
      }
    }

    //resolve again using servlet context if that fails
    for (String nameTemplate : fileNameTemplates) {
      String templateName = String.format(nameTemplate, pageClass.getSimpleName());
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
      return new FileInputStream(new File(context.getRealPath(file)));
    } catch (FileNotFoundException e) {
      return null;
    }
  }
  
  private static InputStream openWebInf(String file, ServletContext context) {
    return open("/WEB-INF/" + file, context);
  }

  //resolves a location for this page class's template (assuming @Show is not present)
  private String resolve(Class<?> pageClass) {
    for (String nameTemplate : fileNameTemplates) {
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
}
