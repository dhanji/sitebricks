package com.google.sitebricks;

import com.google.inject.ImplementedBy;

/**
 * An internal hook to start the Sitebricks application lifecycle.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ImplementedBy(ScanAndCompileBootstrapper.class)
interface Bootstrapper {
    void start();
}
