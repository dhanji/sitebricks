package com.google.sitebricks.client.transport;

import com.google.inject.Inject;
import com.google.inject.TypeLiteral;
import com.thoughtworks.xstream.XStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
class XStreamXmlTransport extends Xml {
  private final XStream xStream;

  @Inject
  public XStreamXmlTransport(XStream xStream) {
    this.xStream = xStream;
  }

  public <T> T in(InputStream in, Class<T> type) throws IOException {
    return type.cast(xStream.fromXML(in));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T in(InputStream in, TypeLiteral<T> type) throws IOException {
    return (T)xStream.fromXML(in);
  }

  public <T> void out(OutputStream out, Class<T> type, T data) {
    xStream.toXML(data, out);
  }
}