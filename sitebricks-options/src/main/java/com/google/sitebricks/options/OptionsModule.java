package com.google.sitebricks.options;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class OptionsModule extends AbstractModule {
  private final Map<String, String> options;

  private final List<Class<?>> optionClasses = new ArrayList<Class<?>>();

  public OptionsModule(String[] commandLine, Iterable<Map<String, String>> freeOptions) {
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

    for (Map<String, String> freeOptionMap : freeOptions) {
      options.putAll(freeOptionMap);
    }
  }

  public OptionsModule(String[] commandLine) {
    this(commandLine, ImmutableList.<Map<String, String>>of());
  }

  public OptionsModule(Class<?> configClass, String[] commandLine, String yaml) {
    this(commandLine, ImmutableList.<Map<String, String>>of());
    options(configClass);
  }

  public OptionsModule(Iterable<Map<String, String>> freeOptions) {
    this(new String[0], freeOptions);
  }

  public OptionsModule(Properties... freeOptions) {
    this(new String[0], toMaps(freeOptions));
  }

  public OptionsModule(ResourceBundle... freeOptions) {
    this(new String[0], toMaps(freeOptions));
  }

  private static Iterable<Map<String, String>> toMaps(ResourceBundle[] freeOptions) {
    List<Map<String, String>> maps = Lists.newArrayList();
    for (ResourceBundle bundle : freeOptions) {
      Map<String, String> asMap = Maps.newHashMap();
      Enumeration<String> keys = bundle.getKeys();
      while (keys.hasMoreElements()) {
        String key = keys.nextElement();
        asMap.put(key, bundle.getString(key));
      }

      maps.add(asMap);
    }
    return maps;
  }

  private static Iterable<Map<String, String>> toMaps(Properties[] freeOptions) {
    List<Map<String, String>> maps = Lists.newArrayList();
    for (Properties freeOption : freeOptions) {
      maps.add(Maps.fromProperties(freeOption));
    }
    return maps;
  }

  @Override
  protected final void configure() {
    // Analyze options classes.
    for (Class<?> optionClass : optionClasses) {

      // If using abstract classes, detect cglib.
      if (Modifier.isAbstract(optionClass.getModifiers())) {
        try {
          Class.forName("net.sf.cglib.proxy.Enhancer");
        } catch (ClassNotFoundException e) {
          String message = String.format("Cannot use abstract @Option classes unless Cglib is on the classpath, " +
              "[%s] was abstract. Hint: add Cglib 2.0.2 or better to classpath",
              optionClass.getName());
          Logger.getLogger(Options.class.getName()).severe(message);
          addError(message);
        }
      }

      String namespace = optionClass.getAnnotation(Options.class).value();
      if (!namespace.isEmpty())
        namespace += ".";

      // Construct a map that will contain the values needed to back the interface.
      final Map<String, String> concreteOptions =
          new HashMap<String, String>(optionClass.getDeclaredMethods().length);
      boolean skipClass = false;
      for (Method method : optionClass.getDeclaredMethods()) {
        String key = namespace + method.getName();

        String value = options.get(key);

        // Gather all the errors regarding @Options methods that have no specified config.
        if (null == value && Modifier.isAbstract(method.getModifiers())) {
          addError("Option '%s' specified in type [%s] is unavailable in provided configuration",
              key,
              optionClass);
          skipClass = true;
          break;
        }

        // TODO Can we validate that the value is coercible into the return type correctly?
        concreteOptions.put(method.getName(), value);
      }

      if (!skipClass) {
        Object instance;
        if (optionClass.isInterface()) {
          instance = createJdkProxyHandler(optionClass, concreteOptions);
        } else {
          instance = createCglibHandler(optionClass, concreteOptions);
        }

        bindToInstance(optionClass, instance);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void bindToInstance(Class optionClass, Object instance) {
    bind(optionClass).toInstance(instance);
  }

  private Object createJdkProxyHandler(Class<?> optionClass,
                                       final Map<String, String> concreteOptions) {
    final InvocationHandler handler = new InvocationHandler() {
      @Inject
      OptionTypeConverter converter;

      @Override
      public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        return converter.convert(concreteOptions.get(method.getName()), method.getReturnType());
      }
    };
    requestInjection(handler);
    return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
        new Class<?>[]{optionClass}, handler);
  }

  private Object createCglibHandler(Class<?> optionClass,
                                    final Map<String, String> concreteOptions) {
    MethodInterceptor interceptor = new MethodInterceptor() {
      @Inject
      OptionTypeConverter converter;

      @Override
      public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy)
          throws Throwable {
        String value = concreteOptions.get(method.getName());
        if (null == value) {
          // Return the default value by calling the original method.
          return methodProxy.invokeSuper(o, objects);
        }
        return converter.convert(value, method.getReturnType());
      }
    };
    requestInjection(interceptor);
    return Enhancer.create(optionClass, interceptor);
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
