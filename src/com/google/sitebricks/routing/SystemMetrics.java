package com.google.sitebricks.routing;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.compiler.CompileError;

import java.util.List;

/**
 * Keeps track of various global performance and error metrics.
 * 
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(DefaultSystemMetrics.class)
public interface SystemMetrics {
    /**
     * Records the last page render time for the given page (in millis).
     * This method is concurrent and does not guarantee that the last
     * render time accurately reflects the last page delivered to a user.
     */
    void logPageRenderTime(Class<?> page, long time);

    /**
     * This sets the current errors and warnings list as given, globally.
     * This method is thread-safe.
     */
    void logErrorsAndWarnings(Class<?> page, List<CompileError> errors, List<CompileError> warnings);

    /**
     * Puts the system into a ready state. This is used by Sitebricks to
     * determine whether we're in the compile phase.
     */
    void activate();

    /**
     * @return Returns true if the application is ready to begin processing
     * requests, false if Sitebricks is still in the compile phase.
     */
    boolean isActive();
}
