package com.google.sitebricks.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.jcip.annotations.NotThreadSafe;

import com.ning.http.client.Response;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author Jeanfrancois Arcand (jfarcand@apache.org)
 */
@NotThreadSafe
class WebResponseImpl implements WebResponse {
  
  private Transport transport;
  private final Response response;

  // memo field
  private Map<String, String> headers;

  public WebResponseImpl(Transport transport, Response response) {
    this.transport = transport;
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
    return new ResponseTransportBuilder<T>() {
      public T using(Class<? extends Transport> transportKey) {
        InputStream in = null;
        try {
          in = response.getResponseBodyAsStream();
          return transport.in(in, data);
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
