package com.google.sitebricks;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.compiler.TemplateCompiler;

import java.util.Map;
import java.util.Set;

@Singleton
public class DefaultTemplateSystem implements TemplateSystem {

  private Map<String, TemplateCompiler> templateCompilers;

  @Inject
  public DefaultTemplateSystem(Map<String, TemplateCompiler> templateCompilers) {
    this.templateCompilers = templateCompilers;
  }

  @Override
  public TemplateCompiler compilerFor(String templateName) {
    String extension = templateName.substring(templateName.lastIndexOf(".") + 1);
    return templateCompilers.get(extension);
  }

  @Override
  public String[] getTemplateExtensions() {
      Set<String> keys = templateCompilers.keySet();

      if (keys.isEmpty()) {
          return new String[0];
      }

      String[] extensions = new String[keys.size()];

      int i = 0;
      for (String ext : keys) {
         extensions[i++] = "%s." + ext;
      }

      return extensions;
  }  
}