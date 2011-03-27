package com.google.sitebricks.client.transport;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.client.Transport;

/**
 * A raw implementation of Transport where input types are assumed
 * to be byte arrays.
 *
 * @author dhanji@google.com (Dhanji R. Prasanna)
 */
@ImplementedBy(ByteArrayTransport.class)
public abstract class Raw implements Transport {

  public String contentType() {
    return "application/octet-stream";
  }
}