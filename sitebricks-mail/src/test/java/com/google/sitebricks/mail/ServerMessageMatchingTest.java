package com.google.sitebricks.mail;

import org.testng.annotations.Test;

import java.util.regex.Matcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class ServerMessageMatchingTest {

  @Test
  public final void authFailed() {
    String failure = ". NO [AUTHENTICATIONFAILED] Invalid credentials (Failure)";
    Matcher matcher = MailClientHandler.COMMAND_FAILED_REGEX.matcher(failure);

    assertTrue(matcher.find());
    assertEquals(matcher.group(2), "[AUTHENTICATIONFAILED] Invalid credentials (Failure)");
  }
}
