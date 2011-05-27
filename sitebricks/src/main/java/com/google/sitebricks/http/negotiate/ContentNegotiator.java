package com.google.sitebricks.http.negotiate;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.headless.Request;

import java.util.Map;

/**
 * The strategy for deciding if a Java request handler method, annotated with content
 * negotiation metadata should fire against the given http request.
 *
 * The default strategy compares the value of the annotation to the value(s) of the header
 * exactly.
 */
@ImplementedBy(ExactMatchNegotiator.class)
public interface ContentNegotiator {

  /**
   * Tests whether a given http request (and its headers) should pass against the given
   * map of content negotiation rules.
   *
   * @param negotiations A Map of header names to match expressions (the value part of an
   *  annotation). For example, an annotation {@literal @}Accept("text/html") would produce
   *  a map entry of ["Accept" -> "text/html"], assuming that the annotation is mapped via
   *  the {@linkplain com.google.sitebricks.SitebricksModule#negotiate} method to the "Accept"
   *  http header.
   * @param request The current http request to match against.
   * @return True if the negotiation succeeded on this method.
   */
  boolean shouldCall(Map<String, String> negotiations, Request request);
}
