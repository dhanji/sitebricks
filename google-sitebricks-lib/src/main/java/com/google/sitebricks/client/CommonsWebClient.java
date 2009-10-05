package com.google.sitebricks.client;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Key;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe //@Concurrent
class CommonsWebClient<T> implements WebClient<T> {
  private final Injector injector;
  private final String url;
  private final Map<String, String> headers;
  private final Class<T> transporting;
  private final Key<? extends Transport> transport;
  private final HttpClient httpClient;

  public CommonsWebClient(Injector injector, String url, Map<String, String> headers,
                          Class<T> transporting,
                          Key<? extends Transport> transport) {

    this.injector = injector;

    this.url = url;
    this.headers = (null == headers) ? null : ImmutableMap.copyOf(headers);

    this.transporting = transporting;
    this.transport = transport;

    this.httpClient = new HttpClient();
  }

  private WebResponse simpleRequest(HttpMethodBase methodBase) {

    //set request headers as necessary
    if (null != headers)
      for (Map.Entry<String, String> header : headers.entrySet())
        methodBase.addRequestHeader(header.getKey(), header.getValue());

    try {
      httpClient.executeMethod(methodBase);

      return new WebResponseImpl(injector, methodBase);
    } catch (IOException e) {
      throw new TransportException(e);
    } finally {
      methodBase.releaseConnection();
    }
  }

  private WebResponse request(EntityEnclosingMethod methodBase, T t) {

    //set request headers as necessary
    if (null != headers)
      for (Map.Entry<String, String> header : headers.entrySet())
        methodBase.addRequestHeader(header.getKey(), header.getValue());

    //fire method
    try {

      // Read the entity from the transport plugin.
      final ByteArrayOutputStream stream = new ByteArrayOutputStream();
      injector.getInstance(transport)
          .out(stream, transporting, t);

      // TODO worry about endian issues? Or will Content-Encoding be sufficient?
      final byte[] outBuffer = stream.toByteArray();

      //set request body
      methodBase.setRequestEntity(new ByteArrayRequestEntity(outBuffer));

      httpClient.executeMethod(methodBase);

      return new WebResponseImpl(injector, methodBase);
    } catch (IOException e) {
      throw new TransportException(e);
    } finally {
      methodBase.releaseConnection();
    }
  }

  public WebResponse get() {
    return simpleRequest(new GetMethod(url));
  }

  public WebResponse post(T t) {
    return request(new PostMethod(url), t);
  }

  public WebResponse put(T t) {
    return request(new PutMethod(url), t);
  }

  public WebResponse delete() {
    return simpleRequest(new DeleteMethod(url));
  }
}
