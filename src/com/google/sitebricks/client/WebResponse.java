package com.google.sitebricks.client;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface WebResponse {
  Map<String, String> getHeaders();

  int getStatusCode();

  <T> ResponseTransportBuilder<T> to(Class<T> data);

  String toString();

  /**
   * @author Dhanji R. Prasanna (dhanji@gmail.com)
   */
  public static interface ResponseTransportBuilder<T> {
    T using(Class<? extends Transport> transport);
  }
}
