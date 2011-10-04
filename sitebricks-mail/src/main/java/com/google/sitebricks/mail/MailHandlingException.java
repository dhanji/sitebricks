package com.google.sitebricks.mail;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MailHandlingException extends RuntimeException {
  private final List<String> context;
  private final String message;

  public MailHandlingException(List<String> context, String message, Exception cause) {
    super(cause);
    this.context = context;
    this.message = message;
  }

  public List<String> getContext() {
    return context;
  }

  public String getContextMessage() {
    return message;
  }
}
