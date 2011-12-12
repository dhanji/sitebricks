package com.google.sitebricks;

import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Shutdowner {
  private final Injector injector;

  @Inject
  public Shutdowner(Injector injector) {
    this.injector = injector;
  }

  public void shutdown() {
    List<Binding<Aware>> bindings = injector.findBindingsByType(Bootstrapper.AWARE_TYPE);

    for (Binding<Aware> binding : bindings) {
      injector.getInstance(binding.getKey()).shutdown();
    }
  }
}
