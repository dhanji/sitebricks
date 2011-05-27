package com.google.sitebricks.routing;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;

import java.util.Map;

/**
 * A simple action that takes a request and returns a Reply which
 * a subclass needs to provide.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public abstract class ServiceAction implements Action {
  @Inject
  private Provider<Request> requestProvider;

  @Override
  public boolean shouldCall(Request request) {
    return true;
  }

  @Override
  public final Object call(Object page, Map<String, String> map) {
    return call(requestProvider.get(), map);
  }

  protected abstract Reply<?> call(Request request, Map<String, String> pathFragments);
}
