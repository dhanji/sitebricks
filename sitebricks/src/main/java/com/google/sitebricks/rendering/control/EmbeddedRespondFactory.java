package com.google.sitebricks.rendering.control;

import com.google.sitebricks.Respond;
import com.google.sitebricks.StringBuilderRespond;
import net.jcip.annotations.Immutable;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@Immutable class EmbeddedRespondFactory {
  private final Respond respond = new StringBuilderRespond(new Object());

  public EmbeddedRespond get(Map<String, ArgumentWidget> arguments) {
    return new EmbeddedRespond(arguments, respond);
  }
}
