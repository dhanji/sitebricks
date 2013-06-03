package com.google.sitebricks.routing;

import com.google.sitebricks.ActionDescriptor;
import com.google.sitebricks.headless.Request;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * An Action that is configured using the at().perform() scheme. See
 * {@link com.google.sitebricks.SitebricksModule} for details.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SpiAction implements Action {
  private final Action action;
  private final Map<String, String> selectParams;
  private final Map<String,String> selectHeaders;

  public SpiAction(Action action, ActionDescriptor actionDescriptor) {
    this.action = action;
    selectParams = actionDescriptor.getSelectParams();
    selectHeaders = actionDescriptor.getSelectHeaders();
  }

  @Override
  public boolean shouldCall(Request request) {
    boolean should;

    if (null != selectParams) {
      for (Map.Entry<String, String> select : selectParams.entrySet()) {
        if (!select.getValue().equals(request.param(select.getKey()))) {
          should = false;
        }
      }
    }

    if (null != selectHeaders) {
      for (Map.Entry<String, String> header : selectHeaders.entrySet()) {
        if (!header.getValue().equals(request.header(header.getKey()))) {
          should = false;
        }
      }
    }

    // (JFA) Might be a good idea to pass the value of should as a request attribute
    // so an action can see if what was the value before getting invoked and take a decision based on it.
    should = action.shouldCall(request);  

    return should;
  }

  @Override
  public Object call(Request request, Object page, Map<String, String> map) throws IOException {
    return action.call(request, page, map);
  }

  @Override
  public Method getMethod() {
    return null;
  }

}
