package com.google.sitebricks.routing;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Request;
import net.jcip.annotations.Immutable;

import java.net.URLEncoder;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Immutable @Singleton
class PageBasedRedirect implements Redirect {
  @Inject private PageBook pageBook;
  @Inject private Provider<Request<String>> request;

  @VisibleForTesting
  void setRequestProvider(Provider<Request<String>> request) {
    this.request = request;
  }

  @Override @SuppressWarnings("deprecation") // For URL encoder.
  public String to(Class<?> pageClass, Map<String, String> parameters) {
    At at = pageClass.getAnnotation(At.class);

    String uriTemplate;
    if (at == null) {
      // Fall back to see if this class was registered some other way (i.e. at().show())
      PageBook.Page page = pageBook.forClass(pageClass);

      if (page == null)
        throw new IllegalArgumentException("No such page class was registered (missing @At annotation?): "
            + pageClass.getName());

      uriTemplate = page.getUri();
    } else
      uriTemplate = at.value();

    // Contextualize this request if necessary.
    if (uriTemplate.startsWith("/"))
      uriTemplate = request.get().context() + uriTemplate;

    String[] split = uriTemplate.split("/");
    StringBuilder uri = new StringBuilder();
    for (int i = 1 /* skip the first '/' */, splitLength = split.length; i < splitLength; i++) {
      String piece = split[i];
      uri.append('/');

      if (piece.startsWith(":")) {
        String value = parameters.get(piece.substring(1));
        if (value == null)
          throw new IllegalArgumentException("Missing parameter " + piece
              + " in URI template for page class: " + pageClass.getName()
              + " '" + uriTemplate + "'");

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
