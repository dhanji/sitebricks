package com.google.sitebricks.client;

import com.google.inject.TypeLiteral;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface WebResponse {
  Map<String, String> getHeaders();

  int status();

  <T> ResponseTransportBuilder<T> to(Class<T> data);

  <T> ResponseTransportBuilder<T> to(TypeLiteral<T> data);

  String toString();

  public static interface ResponseTransportBuilder<T> {
    T using(Class<? extends Transport> transport);
  }
}
