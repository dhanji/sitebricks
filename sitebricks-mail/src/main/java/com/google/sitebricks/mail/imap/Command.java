package com.google.sitebricks.mail.imap;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public enum Command {
  LIST_FOLDERS,
  LIST_MESSAGES,
  FETCH,;

  public static Command response(String message) {
    return LIST_FOLDERS;
  }

  public static Command request(String command) {
    return LIST_FOLDERS;
  }
}
