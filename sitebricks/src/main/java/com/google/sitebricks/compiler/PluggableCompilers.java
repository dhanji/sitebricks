package com.google.sitebricks.compiler;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.*;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.rendering.Decorated;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Singleton
public class PluggableCompilers implements Compilers {
    private final WidgetRegistry registry;
    private final PageBook pageBook;
    private final SystemMetrics metrics;
    private final Map<String, Class<? extends Annotation>> httpMethods;
    private final TemplateLoader loader;
    private final Map<String, CompilerFactory> compilers = new HashMap<String, CompilerFactory>();

    @Inject
    public PluggableCompilers(WidgetRegistry registry, PageBook pageBook, SystemMetrics metrics,
                              @Bricks Map<String, Class<? extends Annotation>> httpMethods, TemplateLoader loader,
                              HtmlTemplateCompilerFactory htmlTemplateCompilerFactory,
                              XmlTemplateCompilerFactory xmlTemplateCompilerFactory,
                              FlatTemplateCompilerFactory flatTemplateCompilerFactory,
                              MvelTemplateCompilerFactory mvelTemplateCompilerFactory,
                              FreemarkerTemplateCompilerFactory freemarkerTemplateCompilerFactory) {
        this.registry = registry;
        this.pageBook = pageBook;
        this.metrics = metrics;
        this.httpMethods = httpMethods;
        this.loader = loader;

        // Add the standard compilers
        register(htmlTemplateCompilerFactory, "default", "html", "xhtml");
        register(xmlTemplateCompilerFactory, "xml");
        register(flatTemplateCompilerFactory, "flat");
        register(mvelTemplateCompilerFactory, "mvel");
        register(freemarkerTemplateCompilerFactory, "fml");
    }

    @Override
    public void register(CompilerFactory compilerFactory, String... extensions) {
        for (String extension : extensions) {
            compilers.put(extension, compilerFactory);
            //compilerFactory.registered(extension);
        }
    }

    @Override
    public Set<String> getRegisteredExtensions() {
        return compilers.keySet();
    }

    // TODO(dhanji): Feedback errors as return rather than throwing.
    public void analyze(Class<?> page) {
        // May move this into a separate class if it starts getting too big.
        analyzeMethods(page.getDeclaredMethods());
        analyzeMethods(page.getMethods());
    }

    private void analyzeMethods(Method[] methods) {
        for (Method method : methods) {
            for (Annotation annotation : method.getDeclaredAnnotations()) {
                // if this is a http method annotation, do some checking on the
                // args and return types.
                if (httpMethods.containsValue(annotation.annotationType())) {
                    Class<?> returnType = method.getReturnType();

                    PageBook.Page page = pageBook.forClass(returnType);
                    if (null == page) {
                        // throw an error.
                    } else {
                        // do further analysis on this sucka
                        if (page.getUri().contains(":"))
                            ; // throw an error coz we cant redir to dynamic URLs


                        // If this is headless, it MUST return an instance of reply.
                        if (page.isHeadless()) {
                            if (!Reply.class.isAssignableFrom(method.getReturnType())) {
                                // throw error
                            }
                        }
                    }
                }
            }
        }
    }

    public void compilePage(PageBook.Page page) {
        // find the template page class
        Class<?> templateClass = page.pageClass();

        // root page uses the last template, extension uses its own embedded template
        if (!page.isDecorated() && templateClass.isAnnotationPresent(Decorated.class)) {
            // the first superclass with a @Show and no @Extension is the template
            while (!templateClass.isAnnotationPresent(Show.class) ||
                    templateClass.isAnnotationPresent(Decorated.class)) {
                templateClass = templateClass.getSuperclass();
                if (templateClass == Object.class) {
                    throw new MissingTemplateException("Could not find tempate for " + page.pageClass() +
                            ". You must use @Show on a superclass of an @Extension page");
                }
            }
        }

        Renderable widget = compile(templateClass);

        //apply the compiled widget chain to the page (completing compile step)
        page.apply(widget);
    }

    @Override
    public Renderable compile(Class<?> templateClass) {
        final Template template = loader.load(templateClass);

        Renderable widget;

        if (compilers.containsKey(template.getExtension())) {
            widget = compilers.get(template.getExtension()).get(templateClass, template, registry, pageBook, metrics).compile(template.getText());
        } else {
            widget = compilers.get("default").get(templateClass, template, registry, pageBook, metrics).compile(template.getText());
        }

        return widget;
    }
}
