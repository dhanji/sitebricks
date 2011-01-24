package com.google.inject.stat;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * This listener registers an {@link InjectionListener} for each type
 * for which it is notified.  When its
 * {@link InjectionListener#afterInjection(Object)} is invoked, a listener
 * registers all annotated fields on the given injectee.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class StatAnnotatedTypeListener implements TypeListener {

  private final StatRegistrar statRegistrar;

  StatAnnotatedTypeListener(StatRegistrar statRegistrar) {
    this.statRegistrar = statRegistrar;
  }

  @Override
  public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
    encounter.register(new InjectionListener<I>() {
      @Override public void afterInjection(I injectee) {
        statRegistrar.registerAllStatsOn(injectee);
      }
    });
  }
}
