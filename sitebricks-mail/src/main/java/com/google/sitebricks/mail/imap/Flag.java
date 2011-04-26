package com.google.sitebricks.mail.imap;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public enum Flag {
  SEEN,
  RECENT;

  public static Flag named(String name) {
    if ("SEEN".equals(name)) {
      return SEEN;
    } else if ("RECENT".equals(name)) {
      return RECENT;
    }
    return null;
  }
}
