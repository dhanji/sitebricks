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
        Parsing.tokenize("7 FETCH (ENVELOPE (\"Sun, 10 Apr 2011 16:38:38 +1000\" " +
            "\"test\" ((\"Scott Bakula\" NIL \"test.account\" \" gmail.  om\")) ((\"Scott " +
            "Bakula\" " +
            "NIL \"agent.bain\" \"gmail.com\")) ((\"Scott Bakula\" NIL \"test.account\" \"gmail" +
            ".com\")) ((NIL NIL \"telnet.imap\" \"gmail.com\")) NIL NIL NIL " +
            "\"<BANLkTimf9tS+aJXa1QLdMiH4OgtdK7k=FQ@mail.gmail.com>\") FLAGS () INTERNALDATE " +
            "\"10-Apr-2011 06:38:59 +0000\" RFC822.SIZE 2005)");

    String[] expected = new String[] { "7", "FETCH", "(", "ENVELOPE", "(", "\"Sun, 10 Apr 2011 16:38:38 +1000\"", "\"test\"", "(", "(",
        "\"Scott Bakula\"", "NIL", "\"test.account\"", "\" gmail.  om\"", ")", ")", "(", "(", "\"Scott Bakula\"",
        "NIL", "\"agent.bain\"", "\"gmail.com\"", ")", ")", "(", "(", "\"Scott Bakula\"", "NIL", "\"test.account\"",
        "\"gmail.com\"", ")", ")", "(", "(", "NIL", "NIL", "\"telnet.imap\"", "\"gmail.com\"", ")", ")", "NIL", "NIL",
        "NIL", "\"<BANLkTimf9tS+aJXa1QLdMiH4OgtdK7k=FQ@mail.gmail.com>\"", ")", "FLAGS", "(", ")",
        "INTERNALDATE", "\"10-Apr-2011 06:38:59 +0000\"", "RFC822.SIZE", "2005", ")" } ;

    assertEquals(new ArrayList<String>(tokens), Arrays.asList(expected));
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
    assertEquals(statuses.size(), 17);
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
      if (!assertion.endsWith("}")) {
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
