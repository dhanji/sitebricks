package com.google.sitebricks.routing;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.Respond;
import com.google.sitebricks.headless.Request;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@ImplementedBy(WidgetRoutingDispatcher.class)
public interface RoutingDispatcher {
    Respond dispatch(Request request, HttpServletResponse response) throws IOException;
}
