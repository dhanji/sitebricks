package com.google.sitebricks.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface Transport {
  /**
   * Reads from a given inputstream and returns an object representing the unmarshalled
   * form of the underlying protocol data.
   *
   * @param in An inputstream to read data from. This stream will NOT be closed
   *  by the implementation of this interface.
   * @param type The type to read in. The method will return an instance of this
   *  type representing the data in the {@code in} stream.
   * @return An instance of {@code type} representing the data in the provided
   *  stream.
   *
   * @throws IOException Thrown if there is an error reading from this stream.
   */
  <T> T in(InputStream in, Class<T> type) throws IOException;

  /**
   * Converts a given object into transportable data and writes it to the provided
   * OutputStream.
   *
   * @param out An open outputstream to write to. This stream will NOT be closed.
   * @param type The type of the data being serialized to the stream.
   * @param data An object representing the data to be written out.

   * @throws IOException Thrown if there is an error writing to this stream.
   */
  <T> void out(OutputStream out, Class<T> type, T data) throws IOException;

  /**
   * Returns the HTTP content type marshalled by this transport. For example, the
   * {@link com.google.sitebricks.client.transport.Text} transport transforms plain
   * strings to and from the HTTP stream. Its content type is {@code text/plain}. This
   * is only a default (or suggested) content type. You may of course, return whatever
   * content type is most suitable if using this transport to deliver web responses.
   *
   * @return A non-empty string representing a HTTP content (mime) type. 
   */
  String contentType();
}
