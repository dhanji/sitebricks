package com.google.sitebricks.client.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author John Patterson (jdpatterson@gmail.com)
 * @author JRodriguez
 */
@Singleton
public class SimpleJacksonJsonTransport extends Json {

  private final ObjectMapper objectMapper;

  @Inject
  public SimpleJacksonJsonTransport() {
    this.objectMapper = new ObjectMapper();
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public <T> T in(InputStream in, Class<T> type) throws IOException {
    return objectMapper.readValue(in, type);
  }

  public <T> void out(OutputStream out, Class<T> type, T data) {
    try {
      objectMapper.writeValue(out, data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
