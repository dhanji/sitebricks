package com.google.sitebricks.client.v2;

import java.io.IOException;

/**
 * An exception thrown when an unexpected exception occurs with the {@link WebProxy} and {@link WebClient}
 */
public class WebException extends RuntimeException {

  private final int statusCode;
  private final String reasonPhrase;

  /**
   * Create an exception with the associated statusCode and reason phrase.
   * 
   * @param statusCode
   *          the HTTP status code
   * @param reasonPhrase
   *          the HTTP reason phrase
   */
  public WebException(int statusCode, String reasonPhrase) {
    super(new IOException(String.format("Server returned status %s with reason phrase %s", statusCode, reasonPhrase)));
    this.statusCode = statusCode;
    this.reasonPhrase = reasonPhrase;
  }

  /**
   * Create an exception with the Throwable cause.
   * 
   * @param cause
   *          a {@link Throwable}
   */
  public WebException(Throwable cause) {
    super(cause);
    this.statusCode = 500;
    this.reasonPhrase = "Server error";
  }

  /**
   * Return the response status code.
   * 
   * @return the response status code.
   */
  public int getStatusCode() {
    return statusCode;
  }

  /**
   * Return the response's reason phrase.
   * 
   * @return the response's reason phrase.
   */
  public String getReasonPhrase() {
    return reasonPhrase;
  }

  @Override
  public String toString() {
    return "WebException{" + "statusCode=" + statusCode + ", reasonPhrase='" + reasonPhrase + '\'' + '}';
  }
}