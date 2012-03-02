package com.google.sitebricks.client.transport;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.client.Transport;

/**
 * A plain text (UTF-8) implementation of Transport where input types are assumed
 * to be Strings.
 *
 * @author dhanji@google.com (Dhanji R. Prasanna)
 */
@ImplementedBy(JacksonJsonTransport.class)
public abstract class Json implements Transport {

  public String contentType() {
    return "application/json";
  }
}