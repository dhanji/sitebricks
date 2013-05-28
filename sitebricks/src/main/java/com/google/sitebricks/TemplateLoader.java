package com.google.sitebricks;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;

import javax.servlet.ServletContext;

import net.jcip.annotations.Immutable;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.compiler.TemplateCompiler;
import com.google.sitebricks.routing.PageBook;

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

  public Renderable compile(PageBook.Page page) {

      Show methodShow = page.getShow();

      Template template = load(page.pageClass(), methodShow);
      TemplateCompiler templateCompiler = templateSystem.compilerFor(template.getName());

      if (templateCompiler == null) {
        templateCompiler = templateSystem.compilerFor("html");
      }

      return templateCompiler.compile(page.pageClass(), template);
    }

  public Renderable compile(Class<?> templateClass) {
      Template template = load(templateClass, null);
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

  // protected for the tests access...
  protected Template load(Class<?> pageClass, Show methodShow) {
    //
    // try to find the template name
    //
    String template = null;
    String extension = null;

    Show show = pageClass.getAnnotation(Show.class);
    if (null != show) {
      template = show.value();
    }
    
    if (methodShow != null) {
        if (template != null) {
            template = template + methodShow.value();
        }
        else {
            template = methodShow.value();
        }
    }

    //
    // an empty string means no template name was given
    //
    if (template == null || template.length() == 0) {
      // use the default name for the page class
      template = pageClass.getSimpleName();
    } else {
      // Check and see if the supplied template value has a supported file extension
      for (String ext : templateSystem.getTemplateExtensions()) {
        String type = ext.replace("%s.", ".");
        if (template.endsWith(type)) {
          extension = type;
          break;
        }
      }
    }

    if (null == template) {
      throw new MissingTemplateException(String.format("Could not determine the base template name for %s", Show.class));
    }

    TemplateSource templateSource = null;
    String text;

    boolean appendExtension = false;
    try {
      final ServletContext servletContext = context.get();
      InputStream stream = null;

      //
      // If there was a matching file extension, short-circuit the deep search
      //

      if (template.contains(".") || null != extension) {
        // Check class neighborhood for direct match
        stream = pageClass.getResourceAsStream(template);

        // Check url conventions for direct match
        if (null == stream) {
          stream = open(template, servletContext);
        }

        // Same as above, but checks in WEB-INF
        if (null == stream) {
          stream = openWebInf(template, servletContext);
        }

        // Finally, try to get the resource from the servlet context internally
        if (null == stream) {
          stream = servletContext.getResourceAsStream(template);
        }
      }

      //
      // No direct match, so start hunting.
      // First, look in class neighborhood for template
      //

      if (null == stream) {
        appendExtension = true;
        for (String ext : templateSystem.getTemplateExtensions()) {
          String name = String.format(ext, template);

          stream = pageClass.getResourceAsStream(name);

          if (null != stream) {
            extension = ext;
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
          String name = String.format(ext, pageClass.getSimpleName());

          stream = open(name, servletContext);

          if (null != stream) {
            extension = ext;
            break;
          }
        }
      }

      //
      // Same thing, but check in WEB-INF
      //

      if (null == stream) {
        for (String ext : templateSystem.getTemplateExtensions()) {
          String name = String.format(ext, pageClass.getSimpleName());

          stream = openWebInf(name, servletContext);

          if (null != stream) {
            extension = ext;
            break;
          }
        }
      }

      //
      // Finally, look in the ServletContext resource path if not in classpath
      //

      if (null == stream) {
        for (String ext : templateSystem.getTemplateExtensions()) {
          String name = String.format(ext, template);

          stream = servletContext.getResourceAsStream(name);

          if (null != stream) {
            extension = ext;
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

    if (appendExtension)
      template += "." + extension;

    return new Template(template, text, templateSource);
    
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
}
