package com.google.sitebricks.headless;

import com.google.common.collect.Multimap;
import com.google.sitebricks.client.Transport;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Sitebricks abstraction of a request. May be a standard HTTP request, a tunneled
 * Sitebricks RPC-over-HTTP, or another abstraction entirely.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface Request {

  /**
   * Reads the raw request data into an object of the given type. Must
   * be followed by a transport clause for correct unmarshalling. Example:
   * <pre>
   *   Person p = request.read(Person.class).as(Json.class);
   * </pre>
   *
   * @param type The target type to unmarshall the raw request data into.
   * @return an instance containing the deserialized raw data.
   */
  <E> RequestRead<E> read(Class<E> type);

  /**
   * Reads the request data directly into the given output stream. Useful
   * for streaming uploads to a file or passthrough socket.
   *
   * @param out Any valid, open outputstream. Not closed after writing.
   *
   * @throws IOException If an error occurs during the streaming.
   */
  void readTo(OutputStream out) throws IOException;

  /**
   * Returns request headers as a multimap (to account for repeated headers).
   */
  Multimap<String, String> headers();

  /**
   * Returns request parameters as a multimap (to account for repeated values).
   */
  Multimap<String, String> params();

  /**
   * Returns matrix parameters as a multimap (to account for repeated values).
   */
  Multimap<String, String> matrix();

  /**
   * Returns the only value of a matrix parameter or null if the parameter
   * was not present.
   */
  String matrixParam(String name);

  /**
   * Returns the only value of a request parameter or null if the parameter
   * was not present.
   * <p>
   * Behaves exactly like {@link javax.servlet.http.HttpServletRequest#getParameter(String)}.
   */
  String param(String name);

  /**
   * Returns the only value of a request header or null if the header
   * was not present.
   * <p>
   * Behaves exactly like {@link javax.servlet.http.HttpServletRequest#getHeader(String)}.
   */
  String header(String name);

  String uri();

  String path();

  String context();

  String method();

  public static interface RequestRead<E> {
    E as(Class<? extends Transport> transport);
  }
}
