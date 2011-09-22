package com.google.sitebricks.mail;

import org.testng.annotations.Test;

import java.util.regex.Matcher;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MailClientHandlerTest {
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
}
