package com.google.sitebricks;

import com.google.inject.ImplementedBy;
import com.google.inject.TypeLiteral;

/**
 * An internal hook to start the Sitebricks application lifecycle.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ImplementedBy(ScanAndCompileBootstrapper.class)
interface Bootstrapper {
  TypeLiteral<Aware> AWARE_TYPE = new TypeLiteral<Aware>(){};

  void start();
}
