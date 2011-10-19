package com.google.sitebricks.client;

import com.google.inject.Injector;
import com.google.inject.Key;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

/**
 * JdkWebClient is a WebClient implementation which uses the standard URL classes
 * that coming with the JDK, to send HTTP requests through the network.
 *
 * @author Miroslav Genov (mgenov@gmail.com)
 */
class JdkWebClient<T> implements WebClient<T> {

  private final Injector injector;
  private final String url;
  private final Map<String, String> headers;
  private final Class<T> transporting;
  private final Key<? extends Transport> transport;

  public JdkWebClient(Injector injector, String url, Map<String, String> headers, Class<T> transporting, Key<? extends Transport> transport) {
    this.injector = injector;
    this.url = url;
    this.headers = headers;
    this.transporting = transporting;
    this.transport = transport;
  }

  @Override
  public WebResponse get() {
    return simpleRequest("GET");
  }

  @Override
  public WebResponse post(T t) {
    return request("POST", t);
  }

  @Override
  public WebResponse put(T t) {
    return request("PUT", t);
  }

  @Override
  public WebResponse delete() {
    return simpleRequest("DELETE");
  }

  @Override
  public void close() {
  }

  private WebResponse simpleRequest(String requestMethod) {
    try {

      URL url = new URL(this.url);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod(requestMethod);
      conn.setUseCaches(false);

      //TODO{mgenov}: Add basic http authentication support

      //set request headers as necessary
      if (null != headers)
        for (Map.Entry<String, String> header : headers.entrySet()) {
          conn.addRequestProperty(header.getKey(), header.getValue());
        }
      return new JdkWebResponse(injector, conn);
    } catch (IOException e) {
      throw new TransportException(e);
    }
  }


  private WebResponse request(String requestMethod, T t) {
    try {
      URL url = new URL(this.url);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod(requestMethod);
      conn.setUseCaches(false);

      // Read the entity from the transport plugin.
      final ByteArrayOutputStream stream = new ByteArrayOutputStream();
      injector.getInstance(transport)
              .out(stream, transporting, t);


      // OOM if the stream is too bug
      final byte[] outBuffer = stream.toByteArray();
      //set request body
      OutputStream ost = conn.getOutputStream();
      ost.write(outBuffer);
      ost.flush();

      return new JdkWebResponse(injector, conn);
    } catch (ProtocolException e) {
      throw new TransportException(e);
    } catch (MalformedURLException e) {
      throw new TransportException(e);
    } catch (IOException e) {
      throw new TransportException(e);
    }
  }


}
