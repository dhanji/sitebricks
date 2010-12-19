package com.google.sitebricks.headless;

import com.google.common.collect.Multimap;
import com.google.sitebricks.client.Transport;

/**
 * Sitebricks abstraction of a request. May be a standard HTTP request, a tunneled
 * Sitebricks RPC-over-HTTP, or another abstraction entirely.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface Request {
  <E> RequestRead<E> read(Class<E> type);

  Multimap<String, String> headers();

  Multimap<String, String> params();

  public static interface RequestRead<E> {
    E as(Class<? extends Transport> transport);
  }
}
