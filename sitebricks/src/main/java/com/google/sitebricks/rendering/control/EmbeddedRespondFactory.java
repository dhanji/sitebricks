package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Respond;
import com.google.sitebricks.StringBuilderRespond;
import net.jcip.annotations.Immutable;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@Immutable class EmbeddedRespondFactory {
    private final ThreadLocal<Respond> respond = new ThreadLocal<>();

    private Respond getRespond() {
        if (respond.get() == null) {
            respond.set(new StringBuilderRespond(new Object()));
        }
        return respond.get();
    }

    public EmbeddedRespond get(Map<String, ArgumentWidget> arguments) {
        return new EmbeddedRespond(arguments, getRespond());
    }
}
