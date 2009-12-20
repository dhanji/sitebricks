package com.google.sitebricks;

import com.google.inject.binder.ScopedBindingBuilder;

import java.lang.annotation.Annotation;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface PageBinder {
  ShowBinder at(String uri);

  EmbedAsBinder embed(Class<?> clazz);

  void bindMethod(String method, Class<? extends Annotation> annotation);
  
  NegotiateWithBinder negotiate(String header);

  static interface NegotiateWithBinder {
    void with(Class<? extends Annotation> ann);
  }

  static interface ShowBinder {
    ScopedBindingBuilder show(Class<?> clazz);
    ScopedBindingBuilder serve(Class<?> clazz);
    void export(String glob);
  }

  static interface EmbedAsBinder {
    ScopedBindingBuilder as(String annotation);
  }
}
