package com.google.sitebricks.client.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class JacksonJsonTransport extends Json {

  public <T> T in(InputStream in, Class<T> type) throws IOException {
    throw new UnsupportedOperationException();
  }

  public <T> void out(OutputStream out, Class<T> type, T data) {
    throw new UnsupportedOperationException();
  }
}