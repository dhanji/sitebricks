package com.google.sitebricks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Stage;
import com.google.inject.servlet.RequestScoped;
import com.google.sitebricks.client.Transport;
import com.google.sitebricks.conversion.MvelConversionHandlers;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.RoutingDispatcher;
import org.apache.commons.io.IOUtils;
import org.mvel2.MVEL;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * Module encapsulates internal bindings for sitebricks. Can be installed multiple times.
 */
class SitebricksInternalModule extends AbstractModule {

  @Override
  protected void configure() {

    //set up MVEL namespace (when jarjar-ed, it will use the repackaged namespace)
    System.setProperty("mvel.namespace",
        MVEL.class.getPackage().getName().replace('.', '/') + "/");

    // Bind default content negotiation annotations
//    install(new ConnegModule()); TODO(dhanji): Fix this--we have to make SitebricksModule multi-installable

    //initialize startup services and routing modules
    install(PageBook.Routing.module());

    //development mode services
    if (Stage.DEVELOPMENT.equals(binder().currentStage())) {
      bind(PageBook.class).to(DebugModePageBook.class);
      bind(RoutingDispatcher.class).to(DebugModeRoutingDispatcher.class);
    }
    
    // use sitebricks converters in mvel
    requestInjection(new MvelConversionHandlers());
  }

  @Override
  public int hashCode() {
    return SitebricksInternalModule.class.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return SitebricksInternalModule.class.isInstance(obj);
  }
  
  @Provides @RequestScoped
  Request provideRequest(final HttpServletRequest servletRequest, final Injector injector) {
    ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();

    @SuppressWarnings("unchecked") // Guaranteed by servlet spec
    Map<String, String[]> parameterMap = servletRequest.getParameterMap();
    for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
      builder.putAll(entry.getKey(), entry.getValue());
    }

    // Build once per request only (so do it here).
    final ImmutableMultimap<String, String> params = builder.build();

    // Do the request headers now.
    builder = ImmutableMultimap.builder();

    @SuppressWarnings("unchecked") // Guaranteed by servlet spec
    Enumeration<String> headerNames = servletRequest.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String header = headerNames.nextElement();

      @SuppressWarnings("unchecked") // Guaranteed by servlet spec
      Enumeration<String> values = servletRequest.getHeaders(header);
      while (values.hasMoreElements()) {
        builder.put(header, values.nextElement());
      }
    }

    final ImmutableMultimap<String, String> headers = builder.build();

    return new Request() {
      @Override
      public <E> RequestRead<E> read(final Class<E> type) {
        return new RequestRead<E>() {
          E memo;

          @Override
          public E as(Class<? extends Transport> transport) {
            try {
              // Only read from the stream once.
              if (null == memo) {
                memo = injector.getInstance(transport).in(servletRequest.getInputStream(), type);
              }
            } catch (IOException e) {
              throw new RuntimeException("Unable to obtain input stream from servlet request" +
                  " (was it already used or closed elsewhere?). Error:\n" + e.getMessage(), e);
            }

            return memo;
          }
        };
      }

      @Override
      public void readTo(OutputStream out) throws IOException {
        IOUtils.copy(servletRequest.getInputStream(), out);
      }

      @Override
      public Multimap<String, String> headers() {
        return headers;
      }

      @Override
      public Multimap<String, String> params() {
        return params;
      }

      @Override
      public String param(String name) {
        return servletRequest.getParameter(name);
      }
    };
  }

  @Provides @RequestScoped
  Locale provideLocale(HttpServletRequest request) {
    return request.getLocale();
  }
}
