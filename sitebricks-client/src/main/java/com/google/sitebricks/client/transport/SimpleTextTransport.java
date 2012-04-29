package com.google.sitebricks.client.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class SimpleTextTransport extends Text {
    public <T> T in(InputStream in, Class<T> type) throws IOException {
      return type.cast(CharStreams.toString(new InputStreamReader(in)));
    }

    public <T> void out(OutputStream out, Class<T> type, T data) {
      try {
        ByteStreams.copy(new ByteArrayInputStream(data.toString().getBytes()), out);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
}
