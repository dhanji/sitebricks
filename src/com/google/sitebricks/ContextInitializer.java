package com.google.sitebricks;

import com.google.inject.Inject;
import net.jcip.annotations.NotThreadSafe;

import javax.servlet.ServletContext;

/**
 * TODO make this hook into the servlet lifecycle and delete this stupid class.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@NotThreadSafe
class ContextInitializer {
    private final Bootstrapper scanner;

    @Inject
    public ContextInitializer(Bootstrapper scanner) {
        this.scanner = scanner;
    }

    public void init(ServletContext servletContext) {

        //start and add pages/sitebricks
        scanner.start();
    }
}
