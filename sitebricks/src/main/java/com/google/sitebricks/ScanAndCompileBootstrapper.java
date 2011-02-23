package com.google.sitebricks;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.sitebricks.SitebricksModule.BindingKind.ACTION;
import static com.google.sitebricks.SitebricksModule.BindingKind.EMBEDDED;
import static com.google.sitebricks.SitebricksModule.BindingKind.PAGE;
import static com.google.sitebricks.SitebricksModule.BindingKind.SERVICE;
import static com.google.sitebricks.SitebricksModule.BindingKind.STATIC_RESOURCE;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Stage;
import com.google.sitebricks.compiler.Compilers;
import com.google.sitebricks.compiler.TemplateCompileException;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.rendering.EmbedAs;
import com.google.sitebricks.rendering.With;
import com.google.sitebricks.rendering.control.WidgetRegistry;
import com.google.sitebricks.rendering.resource.ResourcesService;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.SystemMetrics;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class ScanAndCompileBootstrapper implements Bootstrapper {
  private final PageBook pageBook;
  private final TemplateLoader loader;
  private final List<Package> packages;
  private final ResourcesService resourcesService;
  private final WidgetRegistry registry;
  private final SystemMetrics metrics;
  private final Compilers compilers;
  
  @Inject @Bricks
  private final List<SitebricksModule.LinkingBinder> bindings = null;
  
  @Inject
  private final Stage currentStage = null;

  private final Logger log = Logger.getLogger(
      ScanAndCompileBootstrapper.class.getName());

  @Inject
  public ScanAndCompileBootstrapper(PageBook pageBook, TemplateLoader loader,
                                    @Bricks List<Package> packages,
                                    ResourcesService resourcesService,
                                    WidgetRegistry registry,
                                    SystemMetrics metrics,
                                    Compilers compilers) {

    this.pageBook = pageBook;
    this.loader = loader;
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
      set.addAll(Classes.matching(annotatedWith(At.class)
          .or(annotatedWith(EmbedAs.class).or(annotatedWith(With.class)))
      ).in(pkg));
    }

    //we need to scan all the pages first (do not collapse into the next loop)
    Set<PageBook.Page> pagesToCompile = scanPagesToCompile(set);
    collectBindings(bindings, pagesToCompile);

    // Compile templates for scanned classes (except in dev mode, where faster startup
    // time is more important and compiles are amortized across visits to each page).
    if (Stage.DEVELOPMENT != currentStage) {
      compilePages(pagesToCompile);
    }

    //set application mode to started (now debug mechanics can kick in)
    metrics.activate();
  }

  //processes all explicit bindings, including static resources.
  private void collectBindings(List<SitebricksModule.LinkingBinder> bindings,
                               Set<PageBook.Page> pagesToCompile) {

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
        pageBook.at(binding.uri, binding.actionDescriptor);
      }
    }
  }

  //goes through the set of scanned classes and builds pages out of them.
  private Set<PageBook.Page> scanPagesToCompile(Set<Class<?>> set) {

    Set<PageBook.Page> pagesToCompile = Sets.newHashSet();
    for (Class<?> page : set) {
      if (page.isAnnotationPresent(EmbedAs.class)) {
        final String embedAs = page.getAnnotation(EmbedAs.class).value();

        //is this a text rendering or embedding-style widget?
        if (Renderable.class.isAssignableFrom(page)) {
          //noinspection unchecked
          registry.add(embedAs, (Class<? extends Renderable>) page);
        } else {
          pagesToCompile.add(embed(embedAs, page));
        }
      }

      At at = page.getAnnotation(At.class);
      if (null != at) {
        if (page.isAnnotationPresent(Service.class)) {
          pagesToCompile.add(pageBook.serviceAt(at.value(), page));
        } else if (page.isAnnotationPresent(Export.class)) {
          //localize the resource to the SitebricksModule's package.
          resourcesService.add(SitebricksModule.class, page.getAnnotation(Export.class));
        } else
          pagesToCompile.add(pageBook.at(at.value(), page));
      }
    }

    return pagesToCompile;
  }

  private void compilePages(Set<PageBook.Page> pagesToCompile) {
    final List<TemplateCompileException> failures = Lists.newArrayList();

    //perform a compilation pass over all the pages and their templates
    for (PageBook.Page toCompile : pagesToCompile) {
      Class<?> page = toCompile.pageClass();

      // Headless web services need to be analyzed but not page-compiled.
      if (toCompile.isHeadless()) {
        // TODO(dhanji): Feedback errors as return rather than throwing.
        compilers.analyze(page);
        continue;
      }

      if (log.isLoggable(Level.FINEST)) {
        log.finest("Compiling template for page " + page.getName());
      }

      try {
        final Template template = loader.load(page);

        Renderable widget;

        //is this an HTML, XML, or a flat-file template?
        switch(template.getKind()) {            
          default:
          case HTML:
            widget = compilers.compileHtml(page, template.getText());
            break;
          case XML:
            widget = compilers.compileXml(page, template.getText());
            break;
          case FLAT:
            widget = compilers.compileFlat(page, template.getText());
            break;
          case MVEL:
            widget = compilers.compileMvel(page, template.getText());          
            break;
          case FREEMARKER:
            widget = compilers.compileFreemarker(page, template.getText());
            break;
        }

        compilers.analyze(page);

        //apply the compiled widget chain to the page (completing compile step)
        toCompile.apply(widget);
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
