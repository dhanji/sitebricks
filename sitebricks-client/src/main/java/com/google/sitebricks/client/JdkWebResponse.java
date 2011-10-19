package com.google.sitebricks.client;

import com.google.common.io.ByteStreams;
import com.google.inject.Injector;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * JdkWebResponse is a response which is returned when regular JDK HttpUrlConnection class
 * is used for sending of http requests.
 *
 * @author Miroslav Genov (mgenov@gmail.com)
 */
class JdkWebResponse implements WebResponse {

  private final Injector injector;
  private final HttpURLConnection connection;

  // memo field
  private Map<String, String> headers;

  // memo field which holds the last received string message or transformed object from request
  private Object response;

  public JdkWebResponse(Injector injector, HttpURLConnection connection) {
    this.injector = injector;
    this.connection = connection;
  }

  @Override
  public Map<String, String> getHeaders() {
    if (null != headers) {
      return this.headers;
    }

    Map<String, String> headers = new HashMap<String, String>();
    for (Map.Entry<String, List<String>> header : connection.getHeaderFields().entrySet()) {
      for (String value : header.getValue()) {
        headers.put(header.getKey(), value);
      }
    }
    return this.headers = headers;
  }

  @Override
  public <T> ResponseTransportBuilder<T> to(final Class<T> data) {
    return new ResponseTransportBuilder<T>() {
      @Override
      public T using(Class<? extends Transport> transport) {
        InputStream in = null;
        try {
          in = connection.getInputStream();
          T resp = injector.getInstance(transport).in(in,data);
          response = resp;
          return resp;
        } catch (IOException e) {
          throw new TransportException(e);

          //ugly stream closing here, to abstract it away from user code
        } finally {
          try {
            if (null != in)
              in.close();
          } catch (IOException e) {
            //strange, unrecoverable error =(
            Logger.getLogger(JdkWebResponse.class.getName())
                    .severe("Could not close input stream to in-memory byte array: " + e);
          }
        }
      }
    };
  }

  @Override
  public int status() {
    try {
      return connection.getResponseCode();
    } catch (IOException e) {
      throw new TransportException(e);
    }
  }

  @Override
  public String toString() {
    // we haven't used response transformation, so we could return response as string directly
    if (response == null) {
      InputStream in = null;
      try {
        in = connection.getInputStream();
        response = new String(ByteStreams.toByteArray(connection.getInputStream()));
        return response.toString();
      } catch (IOException e) {
        throw new TransportException(e);
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            throw new TransportException(e);
          }
        }
      }
    }
    return response.toString();
  }
}
