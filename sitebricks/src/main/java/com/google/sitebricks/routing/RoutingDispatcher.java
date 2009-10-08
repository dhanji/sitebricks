package com.google.sitebricks.routing;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.Respond;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ImplementedBy(WidgetRoutingDispatcher.class)
public interface RoutingDispatcher {
    Respond dispatch(HttpServletRequest request);
}
