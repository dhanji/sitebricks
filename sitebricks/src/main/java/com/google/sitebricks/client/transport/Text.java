package com.google.sitebricks.client.transport;

import com.google.sitebricks.client.Transport;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A plain text (UTF-8) implementation of Transport where input types are assumed
 * to be Strings.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class Text implements Transport {
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
