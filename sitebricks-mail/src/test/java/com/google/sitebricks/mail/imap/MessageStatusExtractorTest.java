package com.google.sitebricks.mail.imap;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageStatusExtractorTest {

  @Test
  public final void testTokenizer() throws IOException, ParseException {
    Queue<String> tokens =
        Parsing.tokenize("7 FETCH (UID 99 ENVELOPE (\"Sun, 10 Apr 2011 16:38:38 +1000\" " +
            "\"test\" ((\"Scott Bakula\" NIL \"test.account\" \" gmail.  om\")) ((\"Scott " +
            "Bakula\" " +
            "NIL \"agent.bain\" \"gmail.com\")) ((\"Scott Bakula\" NIL \"test.account\" \"gmail" +
            ".com\")) ((NIL NIL \"telnet.imap\" \"gmail.com\")) NIL NIL NIL " +
            "\"<BANLkTimf9tS+aJXa1QLdMiH4OgtdK7k=FQ@mail.gmail.com>\") FLAGS (\\Flagged \\Seen) INTERNALDATE " +
            "\"10-Apr-2011 06:38:59 +0000\" RFC822.SIZE 2005)");

    String[] expected = new String[] { "7", "FETCH", "(", "UID", "99", "ENVELOPE", "(", "\"Sun, 10 Apr 2011 16:38:38 +1000\"", "\"test\"", "(", "(",
        "\"Scott Bakula\"", "NIL", "\"test.account\"", "\" gmail.  om\"", ")", ")", "(", "(", "\"Scott Bakula\"",
        "NIL", "\"agent.bain\"", "\"gmail.com\"", ")", ")", "(", "(", "\"Scott Bakula\"", "NIL", "\"test.account\"",
        "\"gmail.com\"", ")", ")", "(", "(", "NIL", "NIL", "\"telnet.imap\"", "\"gmail.com\"", ")", ")", "NIL", "NIL",
        "NIL", "\"<BANLkTimf9tS+aJXa1QLdMiH4OgtdK7k=FQ@mail.gmail.com>\"", ")", "FLAGS", "(", "\\Flagged", "\\Seen", ")",
        "INTERNALDATE", "\"10-Apr-2011 06:38:59 +0000\"", "RFC822.SIZE", "2005", ")" } ;

    assertEquals(new ArrayList<String>(tokens), Arrays.asList(expected), "Tokens mismatched, expected: " + expected + " got: " + tokens);
  }

  @Test
  public final void testTokenizerWithDoubleEscaping() throws IOException, ParseException {
    // raw: "\"your website \\\"cenqua.com\\\"\""
    Queue<String> tokens =Parsing.tokenize(
        "ENVELOPE (\"\\\"your website \\\\\\\"fluent.com\\\\\\\"\\\"\") Flags (\\Seen)");

    String[] expected = new String[] { "ENVELOPE", "(", "\"\"your website \\\"fluent.com\\\"\"\"",
        ")", "Flags", "(", "\\Seen", ")" } ;

    assertEquals(new ArrayList<String>(tokens), Arrays.asList(expected), "Tokens mismatched, expected: " + Arrays.asList(expected) + " got: " + tokens);
  }

  @Test
  public final void testTokenizerWithQuotesAndEscaping() throws IOException, ParseException {
    // raw:  ENVELOPE ("backslash quote \\\" and double backslash quote \\\\\" OK?")
    Queue<String> tokens =Parsing.tokenize(
        "ENVELOPE (\"backslash quote \\\\\\\" and double backslash quote \\\\\\\\\\\" OK?\")");

    String[] expected = new String[] { "ENVELOPE", "(", "\"backslash quote \\\" and double backslash quote \\\\\" OK?\"", ")" } ;

    assertEquals(new ArrayList<String>(tokens), Arrays.asList(expected), "Tokens mismatched, expected: " + Arrays.asList(expected) + " got: " + tokens);
  }

  /**
   * WARNING: THIS TEST IS DATA-DEPENDENT!
   */
  @Test
  public final void testTypicalGmailInboxHeaders() throws IOException, ParseException {

    List<String> data =
        Resources.readLines(MessageStatusExtractorTest.class.getResource("fetch_all_data.txt"),
            Charsets.UTF_8);
    List<String> assertions =
        Resources.readLines(MessageStatusExtractorTest.class.getResource("fetch_all_data_assertion.txt"),
            Charsets.UTF_8);

    List<MessageStatus> statuses =
        new MessageStatusExtractor().extract(data);

    MessageStatus status = statuses.get(0);
    assertEquals(statuses.size(), 22);
    assertEquals(EnumSet.noneOf(Flag.class), status.getFlags());
    assertEquals("<BANLkTi=zC_UQExUuaNqiP0dJXoswDej1Ww@mail.gmail.com>", status.getMessageUid());
    assertEquals("Get Gmail on your mobile phone", status.getSubject());

    // Unfold assertions.
    assertions = unfoldAssertionLines(assertions);

    for (int i = 0, statusesSize = statuses.size(); i < statusesSize; i++) {
      MessageStatus st = statuses.get(i);
//      System.out.println(st);
      assertEquals(st.toString(), assertions.get(i));
    }
  }

  private static List<String> unfoldAssertionLines(List<String> assertions) {
    List<String> unfoldedAssertions = Lists.newArrayListWithCapacity(assertions.size());
    for (int i = 0; i < assertions.size(); i++) {
      String assertion = assertions.get(i);
      while (!assertion.endsWith("}")) {
        String next = assertions.get(i + 1);
        if (!next.startsWith("MessageStatus{")) {
          assertion += '\n' + next;
          assertions.remove(i + 1);
        }
      }

      unfoldedAssertions.add(assertion);
    }
    assertions = unfoldedAssertions;
    return assertions;
  }

  @Test
  public final void alternateDateFormatRegex() throws IOException, ParseException {
    assertTrue(MessageStatusExtractor.ALTERNATE_RECEIVED_DATE_PATTERN
        .matcher("10 Sep 2011 14:19:55 -0700").matches());
    assertTrue(MessageStatusExtractor.ALTERNATE_RECEIVED_DATE_PATTERN
        .matcher("1 Sep 2011 14:19:55 -0700").matches());
    assertTrue(MessageStatusExtractor.ALTERNATE_RECEIVED_DATE_PATTERN
        .matcher("1 Sep 2011 04:19:55 +0700").matches());
    assertTrue(MessageStatusExtractor.ALTERNATE_RECEIVED_DATE_PATTERN
        .matcher("1 Jan 1994 04:19:55 0700").matches());

    assertFalse(MessageStatusExtractor.ALTERNATE_RECEIVED_DATE_PATTERN
        .matcher("1st Jan 1994 04:19:55 0700").matches());
    assertFalse(MessageStatusExtractor.ALTERNATE_RECEIVED_DATE_PATTERN
        .matcher("1 Jan 1994 04:19:55 *0700").matches());
    assertFalse(MessageStatusExtractor.ALTERNATE_RECEIVED_DATE_PATTERN
        .matcher("1 Jan 1994 04:19:55").matches());
    assertFalse(MessageStatusExtractor.ALTERNATE_RECEIVED_DATE_PATTERN
        .matcher("1994-Jan-01 04:19:55 0700").matches());
  }
}
