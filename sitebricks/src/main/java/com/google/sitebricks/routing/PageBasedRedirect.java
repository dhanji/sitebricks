package com.google.sitebricks.routing;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import com.google.sitebricks.At;
import net.jcip.annotations.Immutable;

import java.net.URLEncoder;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Immutable @Singleton
class PageBasedRedirect implements Redirect {

  @Override @SuppressWarnings("deprecation") // For URL encoder.
  public String to(Class<?> pageClass, Map<String, String> parameters) {
    At at = pageClass.getAnnotation(At.class);
    if (at == null)
      throw new IllegalArgumentException("No @At annotation found on page class: " + pageClass.getName());

    String[] split = at.value().split("/");
    StringBuilder uri = new StringBuilder();
    for (int i = 1 /* skip the first '/' */, splitLength = split.length; i < splitLength; i++) {
      String piece = split[i];
      uri.append('/');

      if (piece.startsWith(":")) {
        String value = parameters.get(piece.substring(1));
        if (value == null)
          throw new IllegalArgumentException("Missing parameter " + piece
              + " in URI template for page class: " + pageClass.getName()
              + " '" + at.value() + "'");

        uri.append(URLEncoder.encode(value));
      } else
        uri.append(piece);
    }

    return uri.toString();
  }

  @Override
  public String to(Class<?> page) {
    return to(page, ImmutableMap.<String, String>of());
  }
}
