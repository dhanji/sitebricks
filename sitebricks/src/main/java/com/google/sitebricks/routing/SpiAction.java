package com.google.sitebricks.routing;

import com.google.sitebricks.ActionDescriptor;

import javax.servlet.http.HttpServletRequest;
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
  public boolean shouldCall(HttpServletRequest request) {
    boolean should = true;
    if (null != selectParams) {
      for (Map.Entry<String, String> select : selectParams.entrySet()) {
        if (!select.getValue().equals(request.getParameter(select.getKey()))) {
          should = false;
        }
      }
    }

    if (null != selectHeaders) {
      for (Map.Entry<String, String> header : selectHeaders.entrySet()) {
        if (!header.getValue().equals(request.getHeader(header.getKey()))) {
          should = false;
        }
      }
    }
    return should;
  }

  @Override
  public Object call(Object page, Map<String, String> map) {
    return action.call(page, map);
  }
}
