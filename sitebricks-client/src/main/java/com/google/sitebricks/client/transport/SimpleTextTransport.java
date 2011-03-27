package com.google.sitebricks.client.transport;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class SimpleTextTransport extends Text {
    public <T> T in(InputStream in, Class<T> type) throws IOException {
      return type.cast(IOUtils.toString(in));
    }

    public <T> void out(OutputStream out, Class<T> type, T data) {
      try {
        IOUtils.write(data.toString(), out);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
}
