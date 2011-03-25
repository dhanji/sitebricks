package com.google.sitebricks;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Stage;
import com.google.sitebricks.compiler.Compilers;
import com.google.sitebricks.compiler.TemplateCompileException;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.rendering.Decorated;
import com.google.sitebricks.rendering.EmbedAs;
import com.google.sitebricks.rendering.Templates;
import com.google.sitebricks.rendering.With;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.rendering.resource.ResourcesService;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.PageBook.Page;
import com.google.sitebricks.routing.SystemMetrics;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.sitebricks.SitebricksModule.BindingKind.ACTION;
import static com.google.sitebricks.SitebricksModule.BindingKind.EMBEDDED;
import static com.google.sitebricks.SitebricksModule.BindingKind.PAGE;
import static com.google.sitebricks.SitebricksModule.BindingKind.SERVICE;
import static com.google.sitebricks.SitebricksModule.BindingKind.STATIC_RESOURCE;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class ScanAndCompileBootstrapper implements Bootstrapper {
  private final PageBook pageBook;
  private final List<Package> packages;
  private final ResourcesService resourcesService;
  private final WidgetRegistry registry;
  private final SystemMetrics metrics;
  private final Compilers compilers;

  @Inject
  private final Templates templates = null;
  
  @Inject @Bricks
  private final List<SitebricksModule.LinkingBinder> bindings = null;

  @Inject @Bricks
  private final Map<String, Class<? extends Annotation>> methodMap = null;

  @Inject
  private final Stage currentStage = null;

  private final Logger log = Logger.getLogger(ScanAndCompileBootstrapper.class.getName());

  @Inject
  public ScanAndCompileBootstrapper(PageBook pageBook,
                                    @Bricks List<Package> packages,
                                    ResourcesService resourcesService,
                                    WidgetRegistry registry,
                                    SystemMetrics metrics,
                                    Compilers compilers) {

    this.pageBook = pageBook;
    this.packages = packages;
    this.resourcesService = resourcesService;
    this.registry = registry;

    this.metrics = metrics;
    this.compilers = compilers;
  }

  public void start() {
    Set<Class<?>> set = Sets.newHashSet();
    for (Package pkg : packages) {

      //look for any classes annotated with @At, @EmbedAs and @With
      set.addAll(Classes.matching(
    		  annotatedWith(At.class).or(
    		  annotatedWith(EmbedAs.class)).or(
    		  annotatedWith(With.class)).or(
          annotatedWith(Show.class))
      ).in(pkg));
    }

    //we need to scan all the pages first (do not collapse into the next loop)
    Set<PageBook.Page> pagesToCompile = scanPagesToCompile(set);
    collectBindings(bindings, pagesToCompile);
    extendedPages(pagesToCompile);

    // Compile templates for scanned classes (except in dev mode, where faster startup
    // time is more important and compiles are amortized across visits to each page).
    // TODO make this configurable separately to stage for GAE
    if (Stage.DEVELOPMENT != currentStage) {
      compilePages(pagesToCompile);
    }

    //set application mode to started (now debug mechanics can kick in)
    metrics.activate();
  }

  private void extendedPages(Set<Page> pagesToCompile) {
    for (Page page : pagesToCompile) {
      if (page.pageClass().isAnnotationPresent(Decorated.class)) {
        // recursively add extension pages
        analyseExtension(pagesToCompile, page.pageClass());
      }
    }
  }

  //processes all explicit bindings, including static resources.
  private void collectBindings(List<SitebricksModule.LinkingBinder> bindings,
                               Set<PageBook.Page> pagesToCompile) {

    // Reverse the method map for easy lookup of HTTP method annotations.
    Map<Class<? extends Annotation>, String> methodSet = null;

    //go thru bindings and obtain pages from them.
    for (SitebricksModule.LinkingBinder binding : bindings) {

      if (EMBEDDED == binding.bindingKind) {
        if (null == binding.embedAs) {
          // This can happen if embed() is not followed by an .as(..)
          throw new IllegalStateException("embed() missing .as() clause: " + binding.pageClass);
        }
        registry.addEmbed(binding.embedAs);
        pagesToCompile.add(pageBook.embedAs(binding.pageClass, binding.embedAs));

      } else if (PAGE == binding.bindingKind) {
        pagesToCompile.add(pageBook.at(binding.uri, binding.pageClass));

      } else if (STATIC_RESOURCE == binding.bindingKind) {
        //localize the resource to the SitebricksModule's package.
        resourcesService.add(SitebricksModule.class, binding.getResource());
      } else if (SERVICE == binding.bindingKind) {
        pagesToCompile.add(pageBook.serviceAt(binding.uri, binding.pageClass));
      } else if (ACTION == binding.bindingKind) {
        // Lazy create this inverse lookup map, once.
        if (null == methodSet) {
          methodSet = HashBiMap.create(methodMap).inverse();
        }
        pageBook.at(binding.uri, binding.actionDescriptors, methodSet);
      }
    }
  }

  //goes through the set of scanned classes and builds pages out of them.
  private Set<PageBook.Page> scanPagesToCompile(Set<Class<?>> set) {
    Set<Templates.Descriptor> templates = Sets.newHashSet();
    Set<PageBook.Page> pagesToCompile = Sets.newHashSet();
    for (Class<?> pageClass : set) {
      EmbedAs embedAs = pageClass.getAnnotation(EmbedAs.class);
      if (null != embedAs) {
        final String embedName = embedAs.value();
      
        //is this a text rendering or embedding-style widget?
        if (Renderable.class.isAssignableFrom(pageClass)) {
          @SuppressWarnings("unchecked")
          Class<? extends Renderable> renderable = (Class<? extends Renderable>) pageClass;
          registry.add(embedName, renderable);
        } else {
          pagesToCompile.add(embed(embedName, pageClass));
        }
      }
      
      At at = pageClass.getAnnotation(At.class);
      if (null != at) {
        if (pageClass.isAnnotationPresent(Service.class)) {
          pagesToCompile.add(pageBook.serviceAt(at.value(), pageClass));
        } else if (pageClass.isAnnotationPresent(Export.class)) {
          //localize the resource to the SitebricksModule's package.
          resourcesService.add(SitebricksModule.class, pageClass.getAnnotation(Export.class));
        }
        else {
          pagesToCompile.add(pageBook.at(at.value(), pageClass));
        }
      }

      if (pageClass.isAnnotationPresent(Show.class)) {
        // This has a template associated with it.
        templates.add(new Templates.Descriptor(pageClass,
            pageClass.getAnnotation(Show.class).value()));
      }
    }

    // Eagerly load all detected templates in production mode.
    if (Stage.DEVELOPMENT != currentStage) {
      this.templates.loadAll(templates);
    }

    return pagesToCompile;
  }

  private void analyseExtension(Set<PageBook.Page> pagesToCompile, Class<?> extendClass) {
    // store the page with a special page name used by ExtendWidget
    pagesToCompile.add(pageBook.decorate(extendClass));
    
    // recursively analyse super class
    while (extendClass != Object.class) {
      extendClass = extendClass.getSuperclass();
      if (extendClass.isAnnotationPresent(Decorated.class)) {
        analyseExtension(pagesToCompile, extendClass);
      }
      else if (extendClass.isAnnotationPresent(Show.class)) {
        // there is a @Show with no @Extension so this is the outer template
        return;
      }
    }
    throw new IllegalStateException("Could not find super class annotated with @Show");
  }

  private void compilePages(Set<PageBook.Page> pagesToCompile) {
    final List<TemplateCompileException> failures = Lists.newArrayList();

    //perform a compilation pass over all the pages and their templates
    for (PageBook.Page page : pagesToCompile) {
      Class<?> pageClass = page.pageClass();

      // Headless web services need to be analyzed but not page-compiled.
      if (page.isHeadless()) {
        // TODO(dhanji): Feedback errors as return rather than throwing.
        compilers.analyze(pageClass);
        continue;
      }

      if (log.isLoggable(Level.FINEST)) {
        log.finest("Compiling template for page " + pageClass.getName());
      }

      try {
        compilers.compilePage(page);
        compilers.analyze(pageClass);
      } catch (TemplateCompileException e) {
        failures.add(e);
      }
    }

    //log failures if any (we don't abort the app startup)
    if (!failures.isEmpty()) {
      logFailures(failures);
    }
  }

  private PageBook.Page embed(String embedAs, Class<?> page) {
    //store custom page wrapped as an embed widget
    registry.addEmbed(embedAs);

    //store argument name(s) wrapped as an Argument (multiple aliases allowed)
    if (page.isAnnotationPresent(With.class)) {
      for (String callWith : page.getAnnotation(With.class).value()) {
        registry.addArgument(callWith);
      }
    }

    //...add as an unbound (to URI) page
    return pageBook.embedAs(page, embedAs);
  }

  private void logFailures(List<TemplateCompileException> failures) {
    StringBuilder builder = new StringBuilder();
    for (TemplateCompileException failure : failures) {
      builder.append(failure.getMessage());
      builder.append("\n\n");
    }

    log.severe(builder.toString());
  }
}
