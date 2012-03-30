package com.google.sitebricks.client;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Realm;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import net.jcip.annotations.ThreadSafe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * @author Jeanfrancois Arcand (jfarcand@apache.org)
 */
@ThreadSafe
class AHCWebClient<T> implements WebClient<T> {
  private final Injector injector;
  private final String url;
  private final Map<String, String> headers;
  private final Class<T> transporting;
  private final Key<? extends Transport> transport;
  private final AsyncHttpClient httpClient;

  private final Web.Auth authType;
  private final String username;
  private final String password;

  public AHCWebClient(Injector injector, Web.Auth authType, String username, String password, String url, Map<String, String> headers, Class<T> transporting, Key<? extends Transport> transport) {

    this.injector = injector;

    this.url = url;
    this.headers = (null == headers) ? null : ImmutableMap.copyOf(headers);

    this.authType = authType;
    this.username = username;
    this.password = password;
    this.transporting = transporting;
    this.transport = transport;

    // configure auth
    AsyncHttpClientConfig.Builder c = new AsyncHttpClientConfig.Builder();
    if (null != authType) {
      Realm.RealmBuilder b = new Realm.RealmBuilder();
      // TODO: Add support for Kerberos and SPNEGO
      Realm.AuthScheme scheme = authType.equals(Web.Auth.BASIC) ? Realm.AuthScheme.BASIC : Realm.AuthScheme.DIGEST;
      b.setPrincipal(username).setPassword(password).setScheme(scheme);
      c.setRealm(b.build());
    }

    this.httpClient = new AsyncHttpClient(c.build());

  }

  private WebResponse simpleRequest(RequestBuilder requestBuilder) {

    // set request headers as necessary
    if (null != headers)
      for (Map.Entry<String, String> header : headers.entrySet())
        requestBuilder.addHeader(header.getKey(), header.getValue());

    try {

      Response r = httpClient.executeRequest(requestBuilder.build()).get();

      return new WebResponseImpl(injector, r);
    } catch (IOException e) {
      throw new TransportException(e);
    } catch (InterruptedException e) {
      throw new TransportException(e);
    } catch (ExecutionException e) {
      throw new TransportException(e);
    }
  }

  private WebResponse request(RequestBuilder requestBuilder, T t) {

    // set request headers as necessary
    if (null != headers)
      for (Map.Entry<String, String> header : headers.entrySet())
        requestBuilder.addHeader(header.getKey(), header.getValue());

    // fire method
    try {

      // Read the entity from the transport plugin.
      final ByteArrayOutputStream stream = new ByteArrayOutputStream();
      injector.getInstance(transport).out(stream, transporting, t);

      // TODO worry about endian issues? Or will Content-Encoding be sufficient?
      // OOM if the stream is too bug
      final byte[] outBuffer = stream.toByteArray();

      // set request body
      requestBuilder.setBody(outBuffer);

      Response r = httpClient.executeRequest(requestBuilder.build()).get();

      return new WebResponseImpl(injector, r);
    } catch (IOException e) {
      throw new TransportException(e);
    } catch (InterruptedException e) {
      throw new TransportException(e);
    } catch (ExecutionException e) {
      throw new TransportException(e);
    }
  }

  public WebResponse get() {
    return simpleRequest((new RequestBuilder("GET")).setUrl(url));
  }

  public WebResponse post(T t) {
    return request((new RequestBuilder("POST")).setUrl(url), t);
  }

  public WebResponse put(T t) {
    return request((new RequestBuilder("PUT")).setUrl(url), t);
  }

  public WebResponse delete() {
    return simpleRequest((new RequestBuilder("DELETE")).setUrl(url));
  }

  @Override
  public void close() {
    httpClient.close();
  }
}
