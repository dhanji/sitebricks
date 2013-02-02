package com.google.sitebricks.transport;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.client.Transport;

/**
 * 
 */
@ImplementedBy(UrlEncodedFormTransport.class)
public abstract class Form implements Transport {

  public String contentType() {
    return "application/x-www-form-urlencoded";
  }

}
