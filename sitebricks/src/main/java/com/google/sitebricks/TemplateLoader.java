package com.google.sitebricks;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.compiler.TemplateCompiler;
import net.jcip.annotations.Immutable;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.Arrays;

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
        String template = null;
        String extension = null;

        Show show = pageClass.getAnnotation(Show.class);
        if (null != show) {
            template = show.value();
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

                // Finally, try to get the resource from the servlet context
                if (null == stream) {
                    stream = servletContext.getResourceAsStream(template);
                }
            }

            //
            // No direct match, so start hunting.
            // First, look in class neighborhood for template
            //

            if (null == stream) {
                for (String ext : templateSystem.getTemplateExtensions()) {
                    String name = String.format(ext, template);

                    stream = pageClass.getResourceAsStream(name);

                    if (null != stream) {
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
                        break;
                    }
                }
            }

            //
            //if there's still no template, then error out
            //
            if (null == stream) {
                throw new MissingTemplateException(String.format("Could not find a suitable template for %s, " + "did you remember to place an @Show? None of " +
                        Arrays.toString(templateSystem.getTemplateExtensions()).replace("%s.", ".") + " could be found in either package [%s], in the root of the resource dir OR in WEB-INF/.",
                        pageClass.getName(), pageClass.getSimpleName(),
                        pageClass.getPackage().getName()));
            }

            text = read(stream);
        } catch (IOException e) {
            throw new TemplateLoadingException("Could not load template for (i/o error): " + pageClass, e);
        }

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
