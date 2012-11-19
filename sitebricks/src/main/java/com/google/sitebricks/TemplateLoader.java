package com.google.sitebricks;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.compiler.TemplateCompiler;

import net.jcip.annotations.Immutable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.servlet.ServletContext;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
public class TemplateLoader {

  private final Provider<ServletContext> context;
  private final TemplateSystem templateSystem;

  @Inject
  public TemplateLoader(Provider<ServletContext> context, TemplateSystem templateSystem) {
    this.context = context;
    this.templateSystem = templateSystem;
  }

  public Template load(Class<?> pageClass) {
    //
    // try to find the template name
    //
    String name;
    String extension = null;
    String show = null;

    Show showAnnotation = pageClass.getAnnotation(Show.class);
    if (null != showAnnotation) {
      show = showAnnotation.value();
    }

    //
    // an empty string means no template name was given
    //
    if (show == null || show.length() == 0) {
      // use the default name for the page class
      name = pageClass.getSimpleName();
    } else {
      name = show;

      // Check and see if the supplied template value has a supported file extension
      for (String ext : templateSystem.getTemplateExtensions()) {
        String type = ext.replace("%s.", ".");
        if (show.endsWith(type)) {
          name = show.substring(0, show.lastIndexOf(type));
          extension = type.substring(1);
          break;
        }
      }
    }

    if (null == name) {
      throw new MissingTemplateException(String.format("Could not determine the base template name for %s", Show.class));
    }

    TemplateSource templateSource = null;
    String text;

    try {
      final ServletContext servletContext = context.get();
      InputStream stream = null;

      //
      // If there is a direct @Show(template) match, short-circuit the deep search
      //

      if ((null != show && show.contains("."))) {
        // Check class neighborhood for direct match
        stream = pageClass.getResourceAsStream(show);

        // Check url conventions for direct match
        if (null == stream) {
          stream = open(show, servletContext);
        }

        // Same as above, but checks in WEB-INF
        if (null == stream) {
          stream = openWebInf(show, servletContext);
        }

        // Finally, try to get the resource from the servlet context
        if (null == stream) {
          stream = servletContext.getResourceAsStream(show);
        }
      }

      //
      // No direct match, so start hunting.
      // First, look in class neighborhood for template
      //

      if (null == stream) {
        for (String ext : templateSystem.getTemplateExtensions()) {
          String template = String.format(ext, name);

          stream = pageClass.getResourceAsStream(template);

          if (null != stream) {
            extension = ext.substring(3);
            break;
          }
        }
      }

      //
      // Check for qualified file paths in context
      // TODO: I think this is redundant, but make sure before deleting
      //

      if (null == stream) {
        for (String ext : templateSystem.getTemplateExtensions()) {
          String template = String.format(ext, name);

          stream = open(template, servletContext);

          if (null != stream) {
            extension = ext.substring(3);
            break;
          }
        }
      }

      //
      // Same thing, but check in WEB-INF
      //

      if (null == stream) {
        for (String ext : templateSystem.getTemplateExtensions()) {
          String template = String.format(ext, name);

          stream = openWebInf(template, servletContext);

          if (null != stream) {
            extension = ext.substring(3);
            break;
          }
        }
      }

      //
      // Finally, look in the ServletContext resource path if not in classpath
      //

      if (null == stream) {
        for (String ext : templateSystem.getTemplateExtensions()) {
          String template = String.format(ext, name);

          stream = servletContext.getResourceAsStream(template);

          if (null != stream) {
            extension = ext.substring(3);
            break;
          }
        }
      }

      //
      //if there's still no template, then error out
      //
      if (null == stream) {
        throw new MissingTemplateException(String.format(
          "Could not find a suitable template for %s, did you remember to place an @Show? None of " +
          Arrays.toString(templateSystem.getTemplateExtensions()).replace("%s.", ".")
          + " could be found in either package [%s], in the root of the resource dir OR in WEB-INF/.",
          pageClass.getName(), pageClass.getSimpleName(), pageClass.getPackage().getName()));
      }

      text = read(stream);
    } catch (IOException e) {
      throw new TemplateLoadingException("Could not load template for (i/o error): " + pageClass, e);
    }
    return new Template(name + (null != extension ? "." + extension : ""), text, templateSource);
  }

  private static InputStream open(String templateName, ServletContext context) {
    try {
      String path = context.getRealPath(templateName);
      return path == null ? null : new FileInputStream(path);
    } catch (FileNotFoundException e) {
      return null;
    }
  }

  private static InputStream openWebInf(String templateName, ServletContext context) {
    return open("/WEB-INF/" + templateName, context);
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
    if (templateCompiler == null) {
      templateCompiler = templateSystem.compilerFor("html");
    }

    return templateCompiler.compile(templateClass, template);
  }
}
