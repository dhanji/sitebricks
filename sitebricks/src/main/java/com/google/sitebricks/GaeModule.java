package com.google.sitebricks;

import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.google.sitebricks.binding.FlashCache;
import com.google.sitebricks.binding.GaeFlashCache;

/**
 * Sitebricks additional configuration module to work properly in Google Appengine.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class GaeModule extends ServletModule {

  @Override
  protected void configureServlets() {
    bind(FlashCache.class).to(GaeFlashCache.class).in(Singleton.class);

    // Mvel's JIT produces weird security exceptions in GAE because of its flagrant use
    // of sun.misc.Unsafe
    System.setProperty("mvel2.disable.jit", "true");
  }
}
