package com.google.sitebricks.compiler.template;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRegistry;

import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * A thread-safe, concurrent MVEL template registry that automatically loads templates by
 * name from the file system.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SelfLoadingMvelTemplateRegistry implements TemplateRegistry {
  private final ConcurrentMap<String, CompiledTemplate> templates = new MapMaker()
      .makeComputingMap(new Function<String, CompiledTemplate>() {
        @Override public CompiledTemplate apply(String name) {
          return TemplateCompiler.compileTemplate(new File(name + ".mvel"));
        }
      });

  @Override public Iterator iterator() {
    return templates.keySet().iterator();
  }

  @Override public Set<String> getNames() {
    return templates.keySet();
  }

  @Override public boolean contains(String name) {
    return templates.containsKey(name);
  }

  @Override public void addNamedTemplate(String name, CompiledTemplate template) {
    templates.put(name, template);
  }

  @Override public CompiledTemplate getNamedTemplate(String name) {
    return templates.get(name);
  }
}
