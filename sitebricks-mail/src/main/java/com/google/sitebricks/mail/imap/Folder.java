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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Folder)) return false;

    Folder folder = (Folder) o;

    if (name != null ? !name.equals(folder.name) : folder.name != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }
}
