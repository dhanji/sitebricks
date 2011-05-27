package com.google.sitebricks.http.negotiate;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.sitebricks.headless.Request;

import java.util.*;

/**
 * A strategy for deciding whether or not a header is acceptable to the given
 * method map header expressions. This strategy literally matches the value in
 * a header annotation to the value of the given header, and is case sensitive.
 */
class ExactMatchNegotiator implements ContentNegotiator {
  public boolean shouldCall(Map<String, String> negotiations, Request request) {
    Multimap<String, String> headers = request.headers();
    for (Map.Entry<String, String> negotiate : negotiations.entrySet()) {

      Collection<String> collectionOfHeader = headers.get(negotiate.getKey());
      if (null == collectionOfHeader)
        continue;
      Iterator<String> headerValues = collectionOfHeader.iterator();

      // Guaranteed never to throw NPE.
      boolean shouldFire = false;
      while (headerValues.hasNext()) {
        String value = headerValues.next();

        // Everything has to pass for us to say OK.
        shouldFire |= Iterables.contains(Arrays.asList(value.split(",[ ]*")),
            negotiate.getValue());
      }
      if (!shouldFire) {
        return false;
      }
    }

    return true;
  }
}
