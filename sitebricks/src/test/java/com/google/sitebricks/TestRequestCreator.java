package com.google.sitebricks;

import com.google.inject.Injector;
import com.google.inject.util.Providers;
import com.google.sitebricks.headless.Request;

import javax.servlet.http.HttpServletRequest;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class TestRequestCreator {
  public static Request from(HttpServletRequest request, Injector injector) {
    // We give null as validator and validationconverter...
    return new ServletRequestProvider(Providers.of(request), injector, null, null).get();
  }
}
