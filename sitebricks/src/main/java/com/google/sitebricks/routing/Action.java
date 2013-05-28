package com.google.sitebricks.routing;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.sitebricks.headless.Request;

/**
 * An abstract representation of the service code called
 * when a request is processed. Typically maps to a method annotated
 * with @Get or something like that. Can be replaced with a SPI to
 * create dynamic behavior.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface Action {
  /**
   * Returns true if this action should be called on this request.
   * All dispatch rules have succeeded and this is a last-resort
   * gate (for example, to handle special headers or gate IPs etc.).
   */
  boolean shouldCall(Request request);

  /**
   * Invoke this action!
   *
   * @param page The page object on which to call this action. Aka:
   *    the 'resource'.
   * @param map A map of path variables (fragments) to their values.
   * @return an instance of Reply, Redirect or null to trigger a 500 error.
   */
  Object call(Request request, Object page, Map<String, String> map) throws IOException;

  Method getMethod();
  
}
