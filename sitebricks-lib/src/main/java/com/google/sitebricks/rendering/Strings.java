package com.google.sitebricks.rendering;

/**
 * A string-specific set of utilities.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class Strings {
  private Strings() {
  }

  /**
   * Tests for null or emptiness of a string, throwing an
   * {@link IllegalArgumentException} if one is encountered.
   *
   * @param aString Any string to test for emptiness.
   * @param message A message to throw inside an IllegalArgumentException if
   * the {@code aString} was empty.
   */
  public static void nonEmpty(String aString, String message) {
    if (empty(aString))
      throw new IllegalArgumentException(message);
  }

  /**
   * @param string Any string to test for emptiness.
   * @return True if this string is empty or null.
   */
  public static boolean empty(String string) {
    return null == string || "".equals(string.trim());
  }
}
