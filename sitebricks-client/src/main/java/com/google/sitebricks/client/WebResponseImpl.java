package com.google.sitebricks.client;

import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.ning.http.client.Response;
import net.jcip.annotations.NotThreadSafe;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Jeanfrancois Arcand (jfarcand@apache.org)
 */
@NotThreadSafe
class WebResponseImpl implements WebResponse {

  private final Injector injector;
  private final Response response;

  // memo field
  private Map<String, String> headers;

  public WebResponseImpl(Injector injector, Response response) {
    this.injector = injector;
    this.response = response;
  }

  public Map<String, String> getHeaders() {
    if (null != this.headers)
      return this.headers;

    // translate from ahc http client headers
    final Map<String, String> headers = new HashMap<String, String>();
    for (Map.Entry<String, List<String>> header : response.getHeaders().entrySet()) {
      for (String value : header.getValue()) {
        headers.put(header.getKey(), value);
      }
    }

    return this.headers = headers;
  }

  public <T> ResponseTransportBuilder<T> to(final Class<T> data) {
    TypeLiteral<T> typeLiteral = TypeLiteral.get(data);
    return to(typeLiteral);
  }

  @Override
  public <T> ResponseTransportBuilder<T> to(final TypeLiteral<T> data) {
    return new ResponseTransportBuilder<T>() {
      public T using(Class<? extends Transport> transportKey) {
        InputStream in = null;
        try {
          in = response.getResponseBodyAsStream();

          return injector.getInstance(transportKey).in(in, data);
        } catch (IOException e) {
          throw new TransportException(e);
          //
          // ugly stream closing here, to abstract it away from user code
          //
        } finally {
          try {
            if (null != in)
              in.close();
          } catch (IOException e) {
            // strange, unrecoverable error =(
            Logger.getLogger(WebResponseImpl.class.getName()).severe("Could not close input stream to in-memory byte array: " + e);
          }
        }
      }
    };
  }

  public int status() {
    return response.getStatusCode();
  }

  @Override
  public String toString() {
    try {
      return response.getResponseBody();
    } catch (IOException e) {
      // TODO
      return "";
    }
  }
}
