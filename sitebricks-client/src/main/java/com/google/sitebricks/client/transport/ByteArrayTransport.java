package com.google.sitebricks.client.transport;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class ByteArrayTransport extends Xml {

  @SuppressWarnings("unchecked")
  public <T> T in(InputStream in, Class<T> type) throws IOException {
    assert type == byte[].class;
    return (T) IOUtils.toByteArray(in);
  }

  public <T> void out(OutputStream out, Class<T> type, T data) throws IOException {
    assert data instanceof byte[];
    out.write((byte[]) data);
  }
}