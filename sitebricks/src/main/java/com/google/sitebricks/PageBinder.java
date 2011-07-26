package com.google.sitebricks;

import com.google.inject.Key;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.sitebricks.routing.Action;

import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface PageBinder {
  ShowBinder at(String uri);

  EmbedAsBinder embed(Class<?> clazz);

  void bindMethod(String method, Class<? extends Annotation> annotation);

  NegotiateWithBinder negotiate(String header);

  LocalizationBinder localize(Class<?> iface);

  static interface NegotiateWithBinder {
    void with(Class<? extends Annotation> ann);
  }

  static interface ShowBinder extends PerformBinder {
    ScopedBindingBuilder show(Class<?> clazz);
    ScopedBindingBuilder serve(Class<?> clazz);
    void export(String glob);
  }

  static interface PerformBinder {
    ActionBinder perform(Action action);
    ActionBinder perform(Class<? extends Action> action);
    ActionBinder perform(Key<? extends Action> action);
  }

  static interface EmbedAsBinder {
    ScopedBindingBuilder as(String annotation);
  }

  static interface LocalizationBinder {
    void using(Locale locale, Map<String, String> messages);
    void using(Locale locale, Properties messages);
    void using(Locale locale, ResourceBundle messages);
    void usingDefault();
  }

  static interface ActionBinder {
    PerformBinder on(Class<? extends Annotation>... method);
    ActionBinder select(String param, String value);
    ActionBinder selectHeader(String param, String value);
    ActionBinder selectHeader(String param, Pattern regex);
  }
}
