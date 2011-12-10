package com.google.sitebricks.mail;

import com.google.common.collect.ImmutableList;
import com.google.sitebricks.mail.imap.Command;
import com.google.sitebricks.mail.imap.ExtractionException;
import org.testng.annotations.Test;

import java.util.List;
import java.util.regex.Matcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MailClientHandlerTest {
  private void assertProcess(MailClientHandler.InputBuffer ib, String s, List<String> l) {
    assertEquals(ib.processMessage(s), l);
  }

  @Test
  public final void testProcessMessage() {
    // ib is stateful.
    MailClientHandler.InputBuffer ib = new MailClientHandler.InputBuffer();
    assertProcess(ib, "hi ", ImmutableList.<String>of());
    assertProcess(ib, "bob\r\nhow\n", ImmutableList.of("hi bob", "how"));
    assertProcess(ib, "\nis\n\r\n", ImmutableList.of("", "is", ""));
    assertProcess(ib, "your snake\nfeeling ", ImmutableList.of("your snake"));
    assertProcess(ib, "after\neating\nthat", ImmutableList.of("feeling after", "eating"));
    assertProcess(ib, " mushroom?\r\n", ImmutableList.of("that mushroom?"));
  }

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
  public final void testOKSuccessRegex() throws ExtractionException {
    try {
      Command.isEndOfSequence(1L, "1 NO something bad");
      fail("Expected an exception on 'NO' response");
    } catch(ExtractionException ee) {
      // expected;
    }
    assertTrue(Command.isEndOfSequence(1L, "1 OK Success"));
    assertTrue(Command.isEndOfSequence(2L, "2 OK [READ-ONLY] [Gmail]/All Mail selected. (Success)"));
    assertFalse(Command.isEndOfSequence("> OK [READ-ONLY] [Gmail]/All Mail selected. (Success)"));
  }
}
