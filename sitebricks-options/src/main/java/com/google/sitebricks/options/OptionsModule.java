package com.google.sitebricks.options;

import com.google.inject.AbstractModule;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class OptionsModule extends AbstractModule {
  private final Map<String, String> options;

  private final List<Class<?>> optionClasses = new ArrayList<Class<?>>();

  public OptionsModule(String[] commandLine) {
    options = new HashMap<String, String>(commandLine.length);
    for (String option : commandLine) {
      if (option.startsWith("--") && option.length() > 2) {
        option = option.substring(2);

        String[] pair = option.split("=", 2);
        if (pair.length == 1) {
          options.put(pair[0], Boolean.TRUE.toString());
        } else {
          options.put(pair[0], pair[1]);
        }
      }
    }
  }

  @Override
  protected final void configure() {
    // Analyze options classes.
    for (Class<?> optionClass : optionClasses) {
      String namespace = optionClass.getAnnotation(Options.class).value();
      if (!namespace.isEmpty())
        namespace += ".";

      for (Method method : optionClass.getDeclaredMethods()) {
        String key = namespace + method.getName();

        String value = options.get(key);
      }
    }
  }

  public OptionsModule options(Class<?> clazz) {
    if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
      throw new IllegalArgumentException(String.format("%s must be an interface or abstract class",
          clazz.getName()));
    }

    if (!clazz.isAnnotationPresent(Options.class)) {
      throw new IllegalArgumentException(String.format("%s must be annotated with @Options",
          clazz.getName()));
    }

    optionClasses.add(clazz);
    return this;
  }
}
