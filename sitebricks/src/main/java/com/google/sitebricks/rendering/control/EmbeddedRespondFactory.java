package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Respond;
import com.google.sitebricks.StringBuilderRespond;
import net.jcip.annotations.Immutable;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@Immutable class EmbeddedRespondFactory {
  public EmbeddedRespond get(Map<String, ArgumentWidget> arguments, Object parentContext) {
    Respond respond = new StringBuilderRespond(parentContext != null ? parentContext : new Object());
    return new EmbeddedRespond(arguments, respond);
  }
}
