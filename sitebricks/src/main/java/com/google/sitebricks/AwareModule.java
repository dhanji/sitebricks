package com.google.sitebricks;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;

import java.util.UUID;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public abstract class AwareModule extends AbstractModule {
  @Override
  protected final void configure() {
    configureLifecycle();
  }

  protected abstract void configureLifecycle();

  protected ScopedBindingBuilder observe(Class<? extends Aware> aware) {
    Preconditions.checkArgument(!Aware.class.equals(aware), "Can't bind to interface Aware");
    return bind(Key.get(Aware.class, Names.named(UUID.randomUUID().toString()))).to(aware);
  }
}
