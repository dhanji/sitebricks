package com.google.sitebricks.compiler.template;

import com.google.common.collect.ImmutableSet;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.Show;
import org.mvel2.templates.*;

import java.util.Set;

/**
 * Creates renderables, given an MVEL template page.
 */
public class MvelTemplateCompiler {
  private final Class<?> page;
  private TemplateRegistry registry;

  public MvelTemplateCompiler(Class<?> page, TemplateRegistry registry) {
    this.page = page;
    this.registry = registry;
  }

  public Renderable compile(String template) {
    // Compile template immediately.
    final CompiledTemplate compiledTemplate = TemplateCompiler.compileTemplate(template);
    Show show = page.getAnnotation(Show.class);
    if (null != show)
      registry.addNamedTemplate(show.value().replace(".mvel", ""), compiledTemplate);

    return new Renderable() {
      @Override
      public void render(Object bound, Respond respond) {
        assert page.isInstance(bound);
        respond.write(TemplateRuntime.execute(compiledTemplate, bound, registry).toString());
      }

      @Override
      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return ImmutableSet.of();
      }
    };
  }
}
