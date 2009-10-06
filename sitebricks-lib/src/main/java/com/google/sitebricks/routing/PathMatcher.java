package com.google.sitebricks.routing;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
interface PathMatcher {
    boolean matches(String incoming);

    String name();

    Map<String, String> findMatches(String incoming);
}
