package com.google.sitebricks;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.client.Transport;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.http.Parameters;
import org.apache.commons.io.IOUtils;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class NettyRequestProvider implements Provider<Request> {
  private final Provider<HttpRequest> requestProvider;
  private final Injector injector;

  @Inject
  public NettyRequestProvider(Provider<HttpRequest> requestProvider, Injector injector) {
    this.requestProvider = requestProvider;
    this.injector = injector;
  }

  @Override
  public Request get() {
    return new Request() {
      HttpRequest request = NettyRequestProvider.this.requestProvider.get();
      Multimap<String, String> matrix;
      Multimap<String, String> headers;
      Multimap<String, String> params;
      String path;

      // In strict mode, perhaps this should fail?
      // We allow sync and async actions on this request.
      @Override
      public <E> RequestRead<E> read(final Class<E> type) {
        return new RequestRead<E>() {
          E memo;

          @Override
          public E as(Class<? extends Transport> transport) {
            try {

              // Only read from the stream once.
              if (null == memo) {
                memo = injector.getInstance(transport)
                    .in(new ByteArrayInputStream(request.getContent().array()), type);
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
        // Warning this is potentially expensive!
        IOUtils.copy(new ByteArrayInputStream(request.getContent().array()), out);
      }

      @Override
      public <E> AsyncRequestRead<E> readAsync(final Class<E> type) {
        return new AsyncRequestRead<E>() {
          @Override
          public AsyncCompletion<E> as(Class<? extends Transport> transport) {
            return new AsyncCompletion<E>() {
              @Override
              public ListenableFuture<E> future() {
                return null;
              }

              @Override
              public void callback(Object target, String methodName) {
              }

              @Override
              public void callback(Object target, Class<? extends Annotation> methodAnnotatedWith) {
              }
            };
          }
        };
      }

      @Override
      public Multimap<String, String> headers() {
        if (null == headers) {
          // Build once per request only (so do it here).
          ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();

          Set<String> headerNames = request.getHeaderNames();
          for (String headerName : headerNames) {
            for (String headerValue : request.getHeaders(headerName)) {
              builder.put(headerName, headerValue);
            }
          }
          this.headers = builder.build();
        }
        return headers;
      }

      @Override
      public Multimap<String, String> params() {
        if (null == params) {
          readQueryParams();
        }
        return params;
      }

      private void readQueryParams() {
        QueryStringDecoder dec = new QueryStringDecoder(request.getUri());

        // UGH this is awful.
        this.params = Multimaps.newListMultimap(
            Maps.<String, Collection<String>>newHashMap(dec.getParameters()),
            new Supplier<List<String>>() {
              @Override
              public List<String> get() {
                return Lists.newArrayList();
              }
            });

        // TODO(dhanji): for non-GETs we need to progressively build this map with the body too.
      }

      @Override
      public Multimap<String, String> matrix() {
        if (null == this.matrix) {
          this.matrix = Parameters.readMatrix(request.getUri());
        }
        return matrix;
      }

      @Override
      public String matrixParam(String name) {
        if (null == this.matrix) {
          this.matrix = Parameters.readMatrix(request.getUri());
        }
        return Parameters.singleMatrixParam(name, matrix.get(name));
      }

      @Override
      public String param(String name) {
        if (null == params) {
          readQueryParams();
        }
        Collection<String> paramValues = params.get(name);
        if (paramValues == null) {
          return null;
        }
        if (paramValues.size() != 1) {
          throw new IllegalArgumentException("Requested single parameter value but multiple " +
              "values exist for: " + name);
        }
        return paramValues.iterator().next();
      }

      @Override
      public String header(String name) {
        return request.getHeader(name);
      }

      @Override
      public String uri() {
        return request.getUri();
      }

      @Override
      public String path() {
        if (path == null) {
          path = request.getUri();
          int index = path.indexOf("?");
          if (index > -1) {
            return path.substring(0, index);
          }
        }
        return path;
      }

      @Override
      public String context() {
        return null;
      }

      @Override
      public String method() {
        return request.getMethod().getName();
      }
    };
  }
}
