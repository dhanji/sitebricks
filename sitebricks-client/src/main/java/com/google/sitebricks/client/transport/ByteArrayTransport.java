package com.google.sitebricks.client.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.ByteStreams;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class ByteArrayTransport extends Raw {

  @SuppressWarnings("unchecked")
  public <T> T in(InputStream in, Class<T> type) throws IOException {
    assert type == byte[].class;
    return (T) ByteStreams.toByteArray(in);
  }

  public <T> void out(OutputStream out, Class<T> type, T data) throws IOException {
    assert data instanceof byte[];
    out.write((byte[]) data);
  }
}