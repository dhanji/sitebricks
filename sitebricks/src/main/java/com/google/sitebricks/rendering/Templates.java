package com.google.sitebricks.rendering;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Stage;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.StringBuilderRespond;
import com.google.sitebricks.Template;
import com.google.sitebricks.TemplateLoader;
import com.google.sitebricks.compiler.Compilers;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Templates {
  private final TemplateLoader loader;
  private final Compilers compilers;
  private final boolean reloadTemplates;

  private final ConcurrentMap<Class<?>, Renderable> templates = new MapMaker().makeMap();

  @Inject
  public Templates(TemplateLoader loader, Compilers compilers, Stage stage) {
    this.loader = loader;
    this.compilers = compilers;
    this.reloadTemplates = Stage.DEVELOPMENT == stage;
  }

  public void loadAll(Set<Descriptor> templates) {
    // If in production mode, force load all the templates.
    for (Descriptor template : templates) {
      Renderable compiled = compilers.compile(template.clazz);
      Preconditions.checkArgument(null != compiled, "No template found attached to: %s",
          template.clazz);

      this.templates.put(template.clazz, compiled);
    }
  }

  public String render(Class<?> clazz, Object context) {
    Renderable compiled;
    if (reloadTemplates) {
      compiled = compilers.compile(clazz);

      templates.put(clazz, compiled);
    } else {
      compiled = templates.get(clazz);
    }
    Preconditions.checkArgument(null != compiled, "No template found attached to: %s", clazz);

    StringBuilderRespond respond = new StringBuilderRespond();
    //noinspection ConstantConditions
    compiled.render(context, respond);

    return respond.toString();
  }

  public static class Descriptor {
    private final Class<?> clazz;
    private final String fileName;
    private final Template.Kind kind;

    public Descriptor(Class<?> clazz, String fileName) {
      this.clazz = clazz;
      this.fileName = fileName;
      this.kind = Template.Kind.kindOf(fileName);
    }

    public Class<?> getClazz() {
      return clazz;
    }

    public String getFileName() {
      return fileName;
    }

    public Template.Kind getKind() {
      return kind;
    }
  }
}
