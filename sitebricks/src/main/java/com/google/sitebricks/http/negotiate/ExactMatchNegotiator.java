package com.google.sitebricks.http.negotiate;

import com.google.common.collect.Iterables;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

/**
 * A strategy for deciding whether or not a header is acceptable to the given
 * method map header expressions. This strategy literally matches the value in
 * a header annotation to the value of the given header, and is case sensitive.
 */
class ExactMatchNegotiator implements ContentNegotiator {
  public boolean shouldCall(Map<String, String> negotiations, HttpServletRequest request) {
      for (Map.Entry<String, String> negotiate : negotiations.entrySet()) {

        @SuppressWarnings("unchecked") // Guaranteed by servlet spec.
        Enumeration<String> headerValues = request.getHeaders(negotiate.getKey());

        // Guaranteed never to throw NPE.
        boolean shouldFire = false;
        while(headerValues.hasMoreElements()) {
          String value = headerValues.nextElement();

          // Everything has to pass for us to say OK.
          shouldFire |= Iterables.contains(Arrays.asList(value.split(",[ ]*")), negotiate.getValue());
        }
        if (!shouldFire) {
          return false;
        }
      }

      return true;
    }
}
