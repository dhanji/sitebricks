package com.google.sitebricks.headless;

import com.google.common.collect.Multimap;
import com.google.sitebricks.client.Transport;

import java.util.Map;

/**
 * Sitebricks abstraction of a request. May be a standard HTTP request, a tunneled
 * Sitebricks RPC-over-HTTP, or another abstraction entirely.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface Request<E> {
  E as(Class<? extends Transport> transport);

  Map<String, String> headers();

  Multimap<String, String> params();
}
