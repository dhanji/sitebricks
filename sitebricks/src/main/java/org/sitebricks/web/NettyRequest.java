package org.sitebricks.web;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.TypeLiteral;
import com.google.sitebricks.headless.Request;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji (Dhanji R. Prasanna)
 */
public class NettyRequest implements Request {
  private HttpRequest request;

  @Override
  public <E> RequestRead<E> read(Class<E> type) {
    return null;
  }

  @Override
  public <E> RequestRead<E> read(TypeLiteral<E> type) {
    return null;
  }

  @Override
  public void readTo(OutputStream out) throws IOException {

  }

  // memo
  private Multimap<String, String> headers;
  @Override
  public Multimap<String, String> headers() {
    if (this.headers == null) {
      ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
      for (Map.Entry<String, String> header : request.headers()) {
        builder.putAll(header.getKey(), request.headers().getAll(header.getKey()));
      }
      this.headers = builder.build();
    }
    return headers;
  }

  private Multimap<String, String> params;
  @Override
  public Multimap<String, String> params() {
    if (null == params) {
      QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());
      ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
      for (Map.Entry<String, List<String>> p : queryStringDecoder.parameters().entrySet()) {
        builder.putAll(p.getKey(), p.getValue());
      }
      this.params = builder.build();
    }

    return params;
  }

  @Override
  public Multimap<String, String> matrix() {
    return null;
  }

  @Override
  public String matrixParam(String name) {
    return null;
  }

  @Override
  public String param(String name) {
    return params()
        .get(name)
        .stream()
        .findFirst()
        .orElse(null);
  }

  @Override
  public String header(String name) {
    return request.headers().get(name);
  }

  @Override
  public String uri() {
    return request.getUri();
  }

  @Override
  public String path() {
    return null;
  }

  @Override
  public String context() {
    return null;
  }

  @Override
  public String method() {
    return request.getMethod().name();
  }

  @Override
  public void validate(Object obj) {

  }

  public void set(HttpRequest request) {
    this.request = request;
  }
}
