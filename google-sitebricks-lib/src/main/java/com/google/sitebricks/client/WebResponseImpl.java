package com.google.sitebricks.client;

import com.google.inject.Injector;
import net.jcip.annotations.NotThreadSafe;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@NotThreadSafe
class WebResponseImpl implements WebResponse {
  private final Injector injector;
  private final HttpMethodBase method;

  //memo field
  private Map<String, String> headers;
  private final byte[] responseBody;

  public WebResponseImpl(Injector injector, HttpMethodBase method) {
    this.injector = injector;
    this.method = method;

    try {
      responseBody = method.getResponseBody();
    } catch (IOException e) {
      throw new TransportException(e);
    }
  }

  public Map<String, String> getHeaders() {
    if (null != this.headers)
      return this.headers;

    //translate from http client headers & memoize
    final Map<String, String> headers = new HashMap<String, String>();
    for (Header header : method.getResponseHeaders()) {
      headers.put(header.getName(), header.getValue());
    }

    return this.headers = headers;
  }

  public <T> ResponseTransportBuilder<T> to(final Class<T> data) {
    return new ResponseTransportBuilder<T>() {

      public T using(Class<? extends Transport> transport) {

        ByteArrayInputStream in = null; 
        try {

          in = new ByteArrayInputStream(responseBody);
          return injector
              .getInstance(transport)
              .in(in, data);

        } catch (IOException e) {
          throw new TransportException(e);

          //ugly stream closing here, to abstract it away from user code
        } finally {
          try {
            if (null != in)
              in.close();
          } catch (IOException e) {
            //strange, unrecoverable error =(
            Logger.getLogger(WebResponseImpl.class.getName())
                .severe("Could not close input stream to in-memory byte array: " + e);
          }
        }
      }
    };
  }

  public int getStatusCode() {
    return method.getStatusCode();
  }

  @Override
  public String toString() {
    return new String(responseBody);
  }
}
