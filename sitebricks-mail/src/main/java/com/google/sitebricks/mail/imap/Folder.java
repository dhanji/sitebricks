package com.google.sitebricks.mail.imap;

/**
 * Simple data object that represents an IMAP folder.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Folder {
  private final String name;

  public Folder(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
