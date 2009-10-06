package com.google.sitebricks.rendering.control;

import com.google.inject.Inject;
import com.google.sitebricks.Respond;
import net.jcip.annotations.Immutable;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@Immutable
class EmbeddedRespondFactory {
    private final Respond respond;

    @Inject
    public EmbeddedRespondFactory(Respond respond) {
        this.respond = respond;
    }

    public EmbeddedRespond get(Map<String, ArgumentWidget> arguments) {
        return new EmbeddedRespond(arguments, respond);
    }
}
