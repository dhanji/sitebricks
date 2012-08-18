package com.google.sitebricks.routing;

import com.google.inject.ImplementedBy;

import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@ImplementedBy(PageBasedRedirect.class)
public interface Redirect {
  /**
   * Returns a valid URI that can be used to redirect to, based on the target
   * page class supplied and a parameter map of key/value pairs to fill in.
   * So for a page registered at {@code /search/:query} you could obtain a valid
   * URI as follows:
   *
   * <pre>
   *   {@literal @}Inject Redirect redirect;
   *
   *   ...
   *
   *   return redirect.to(SearchPage.class, ImmutableMap.of("query", "sitebricks"));
   * </pre>
   *
   * This will return a redirect to the URI {@code /search/sitebricks}. It will also
   * correctly URL escape any parameter values.
   *
   * @throws IllegalArgumentException if required parameters are missing or if the
   *            page class specified is not a valid Sitebricks class.
   */
  String to(Class<?> page, Map<String, String> parameters);

  /**
   * Same as {@link #to(Class, java.util.Map)} but for pages that have no params. This
   * is the same as returning the page class directly.
   */
  String to(Class<?> page);
}
