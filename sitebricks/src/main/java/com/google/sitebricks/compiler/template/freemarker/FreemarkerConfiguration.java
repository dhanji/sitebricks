package com.google.sitebricks.compiler.template.freemarker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * @author <a href="mailto:rory.cn@gmail.com">Rory</a>
 */
@Singleton
public class FreemarkerConfiguration {
  private final Provider<ServletContext> context;
  
  @Inject
  public FreemarkerConfiguration(Provider<ServletContext> context) {
    this.context = context;
  }
  
  public final Configuration getConfiguration() {
    Configuration configuration = new Configuration();
    InputStream is =  FreemarkerTemplateCompiler.class.getResourceAsStream("/freemarker.properties");
    if (is == null) {
        is =  FreemarkerTemplateCompiler.class.getResourceAsStream("/com/google/sitebricks/compiler/template/freemarker/freemarker.properties");
    }
    Properties properties = new Properties();
      try {
          properties.load(is);
          for (Object key : properties.keySet()) {
            if (StringUtils.equals("template_dir", (String) key)) {
              String value = properties.getProperty((String) key);
              URL url = FreemarkerConfiguration.class.getResource(value);
              if (url == null || !StringUtils.equals("file", url.getProtocol())) {
                if (context != null) {
                  String path = context.get().getRealPath(value);
                  if (path != null) {
                    File dir = new File(path);
                    if (dir.exists() && dir.isDirectory()) {
                      configuration.setDirectoryForTemplateLoading(dir);
                    }
                  }
                }
              } else {
                File dir = new File(url.getFile());
                if (dir.exists() && dir.isDirectory()) {
                  configuration.setDirectoryForTemplateLoading(dir);
                }
              }
            } else {
              configuration.setSetting((String) key, properties.getProperty((String) key));
            }
          }
      } catch (IOException e) {
          //do noting, use default configuration.
      } catch (TemplateException e) {
        throw new RuntimeException("configuration freemarker template error.", e);
      }
    return configuration;
  }
}
