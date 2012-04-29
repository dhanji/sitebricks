package com.google.sitebricks.compiler.template;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.sitebricks.Renderable;
import com.google.sitebricks.Respond;
import com.google.sitebricks.Template;

public abstract class AbstractMagicTemplateCompiler implements MagicTemplateCompiler {
    
  public Renderable compile(final Class<?> page, final Template sitebricksTemplate) {
            
    return new Renderable() {
      @Override
      public void render(Object bound, Respond respond) {
        assert page.isInstance(bound);
        
        //
        // 1. Transform text to XHTML        
        // 2. Process the XHTML however the client wishes
        // 3. Blow the processed XHTML out of pipe
        //
        respond.write(process(page, bound, transform(sitebricksTemplate)));
      }

      @Override
      public <T extends Renderable> Set<T> collect(Class<T> clazz) {
        return ImmutableSet.of();
      }
    };
  }

  public Template transform(Template sitebricksTemplate) {
    return sitebricksTemplate;
  }

  public abstract String process(Class<?> page, Object bound, Template sitebricksTemplate);  
}
