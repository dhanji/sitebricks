package com.google.inject.stat;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class StatAnnotatedTypeListener implements TypeListener {
  private final Stats stats;

  public StatAnnotatedTypeListener(Stats stats) {
    this.stats = stats;
  }

  @Override
  public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
    for (final Field field : type.getRawType().getDeclaredFields()) {
      final Stat stat = field.getAnnotation(Stat.class);

      if (null != stat) {
        encounter.register(new InjectionListener<I>() {
          @Override
          public void afterInjection(I injectee) {
            stats.register(new StatDescriptor(injectee, stat.value(), field));
          }
        });
      }
    }
  }
}
