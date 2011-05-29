package com.google.sitebricks.routing;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.headless.Request;

import java.io.IOException;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ImplementedBy(WidgetRoutingDispatcher.class)
public interface RoutingDispatcher {
  Object dispatch(Request request, Events event) throws IOException;

  public static enum Events {
    BEFORE, DURING, AFTER
  }
}
