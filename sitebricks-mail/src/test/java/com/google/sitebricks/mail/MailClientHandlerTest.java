package com.google.sitebricks.mail;

import com.google.sitebricks.mail.imap.Command;
import org.testng.annotations.Test;

import java.util.regex.Matcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MailClientHandlerTest {
  @Test
  public final void testAuthenticationSuccessRegex() {
    assertTrue(". OK cameron@themaninblue.com Cameron Adams authenticated (Success)"
        .matches("[.] OK .*@.* \\(Success\\)"));
  }

  @Test
  public final void testIdleExistsRegexes() {
    Matcher matcher = MailClientHandler.IDLE_EXISTS_REGEX.matcher("* 46243 EXISTS");
    assertTrue(matcher.matches());
    assertEquals(matcher.group(1), "46243");
  }

  @Test
  public final void testIdleExpungeRegexes() {
    Matcher matcher = MailClientHandler.IDLE_EXPUNGE_REGEX.matcher("* 46243 expunge");
    assertTrue(matcher.matches());
    assertEquals(matcher.group(1), "46243");
  }

  @Test
  public final void testIdleEndedRegexes() {
    Matcher matcher = MailClientHandler.IDLE_ENDED_REGEX.matcher("3 OK IDLE terminated (Success)");
    assertTrue(matcher.matches());
  }

  @Test
  public final void testSystemErrorRegex() {
    assertTrue(MailClientHandler.SYSTEM_ERROR_REGEX.matcher("* bye system error").matches());
    assertTrue(MailClientHandler.SYSTEM_ERROR_REGEX.matcher("* BYE SYSTEM ERROR").matches());
    assertTrue(MailClientHandler.SYSTEM_ERROR_REGEX.matcher("* BYE system error").matches());
    assertTrue(MailClientHandler.SYSTEM_ERROR_REGEX.matcher("*  BYE  system  error").matches());
    assertTrue(MailClientHandler.SYSTEM_ERROR_REGEX.matcher("* BYE SYSTEM error").matches());
    assertTrue(MailClientHandler.SYSTEM_ERROR_REGEX.matcher("* BYE SYSTEM error  \n ").matches());
    assertTrue(MailClientHandler.SYSTEM_ERROR_REGEX.matcher("* BYE SYSTEM error  \t ").matches());
  }

  @Test
  public final void testOKSuccessRegex() {
    assertTrue(Command.isEndOfSequence(1L, "1 OK Success"));
    assertTrue(Command.isEndOfSequence(2L, "2 OK [READ-ONLY] [Gmail]/All Mail selected. (Success)"));
    assertFalse(Command.isEndOfSequence("> OK [READ-ONLY] [Gmail]/All Mail selected. (Success)"));
  }
}
