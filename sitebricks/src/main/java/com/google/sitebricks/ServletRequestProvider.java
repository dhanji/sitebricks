package com.google.sitebricks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.client.Transport;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Parameters;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class ServletRequestProvider implements Provider<Request> {
  private final Provider<HttpServletRequest> servletRequest;
  private final Injector injector;

  @Inject
  public ServletRequestProvider(Provider<HttpServletRequest> servletRequest, Injector injector) {
    this.servletRequest = servletRequest;
    this.injector = injector;
  }

  @Override
  public Request get() {
    return new Request() {
      HttpServletRequest servletRequest = ServletRequestProvider.this.servletRequest.get();
      Multimap<String, String> matrix;
      Multimap<String, String> headers;
      Multimap<String, String> params;
      String method;

      @Override
      public <E> RequestRead<E> read(final Class<E> type) {
        return new RequestRead<E>() {
          E memo;

          @Override
          public E as(Class<? extends Transport> transport) {
            try {
              // Only read from the stream once.
              if (null == memo) {
                memo = injector.getInstance(transport).in(servletRequest.getInputStream(),
                    type);
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
        if (null == headers) {
          readHeaders();
        }
        return headers;
      }

      @Override
      public Multimap<String, String> params() {
        if (null == params) {
          readParams();
        }
        return params;
      }

      @Override
      public Multimap<String, String> matrix() {
        if (null == matrix) {
          this.matrix = Parameters.readMatrix(servletRequest.getRequestURI());
        }
        return matrix;
      }

      @Override
      public String matrixParam(String name) {
        if (null == matrix) {
          this.matrix = Parameters.readMatrix(servletRequest.getRequestURI());
        }
        return Parameters.singleMatrixParam(name, matrix.get(name));
      }

      @Override
      public String param(String name) {
        return servletRequest.getParameter(name);
      }

      @Override
      public String header(String name) {
        return servletRequest.getHeader(name);
      }

      @Override public String uri() {
        return servletRequest.getRequestURI();
      }

      @Override public String path() {
        return servletRequest.getRequestURI().substring(servletRequest.getContextPath().length());
      }

      @Override public String context() {
        return servletRequest.getContextPath();
      }

      @Override public String method() {
        // This ugly hack is required because Sitebricks supports simulating PUT/DELETE requests
        // via browser POST and special form fields.
        if (method == null) {
          String ghostMethod = servletRequest.getParameter(HiddenMethodFilter.hiddenFieldName);
          method = (ghostMethod != null) ? ghostMethod : servletRequest.getMethod();
        }
        return method;
      }

      private void readParams() {
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();

        @SuppressWarnings("unchecked") // Guaranteed by servlet spec
            Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
          builder.putAll(entry.getKey(), entry.getValue());
        }

        this.params = builder.build();
      }

      private void readHeaders() {
        // Build once per request only (so do it here).
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();

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

        this.headers = builder.build();
      }

    };

  }
}
