package com.google.sitebricks.mail.imap;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Queue;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
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

  private void assertUnterminated(boolean expected, String s, boolean alreadyInString) {
    assertEquals(MessageStatusExtractor.isUnterminatedString(s, alreadyInString), expected);
  }

  @Test
  public void testUnterminatedString() throws Exception {
    assertUnterminated(true, "fo\"o", false);
    assertUnterminated(false, "fo\"o", true);

    assertUnterminated(false, "end\" \"start", false);
    assertUnterminated(true, "end\" \"start", true);
    assertUnterminated(false, "end\" \"", false);

    assertUnterminated(false, " \"start\\\"quoted\\\" end\" ", false);
    assertUnterminated(false, " \"start\\\"quote end\" ", false);

    assertUnterminated(true, " \"start\\\"quote end\" \"start", false);

    assertUnterminated(false, "fo\\ \"o", true);
    assertUnterminated(true, "fo\\ \"o", false);

    assertUnterminated(false, " \"start triple backslash quote \\\\\\\" end\"", false);
    assertUnterminated(true, " \"start double backslash quote end\\\\\" \"start", false);
  }

  @Test
  public void testTrickyHeader() throws Exception {
    List<String> header = ImmutableList.of("* 129233 FETCH (X-GM-THRID 66666666666666 " +
        "X-GM-MSGID 66666666666666 X-GM-LABELS (error) UID 666666 RFC822.SIZE " +
        "6757 INTERNALDATE \"20-Sep-2011 13:05:10 +0000\" FLAGS (\\Seen) ENVELOPE (\"Tue, 20 Sep 2011 13:12:09 +0000\" " +
        "\"start on\\\" one line",
        "continue on next (with bracket)",
        " end on second line\" ((NIL NIL \"errorboss\" \"sitebricks.org\")) ((NIL NIL \"errorboss\" \"sitebricks.org\")) " +
            "((NIL NIL \"errorboss\" \"sitebricks.org\")) ((NIL NIL \"errorboss\" \"sitebricks.org\")) NIL NIL NIL" +
            " \"<66666666666666666@foobar.com>\"))");

    MessageStatusExtractor mse = new MessageStatusExtractor();
    List<MessageStatus> statuses = mse.extract(header);
    assertEquals(statuses.size(), 1);
    assertEquals(statuses.get(0).getSubject(),
        "start on\" one line\ncontinue on next (with bracket)\n end on second line");
  }

  @Test
  public final void testSizeMarkerRegex() throws IOException, ParseException {
    assertTrue(MessageStatusExtractor.SIZE_MARKER.matcher("() {01}").find());
    assertTrue(MessageStatusExtractor.SIZE_MARKER.matcher("() {121}\n").find());
    assertTrue(MessageStatusExtractor.SIZE_MARKER.matcher("() {88571}\r\n").find());
    assertFalse(MessageStatusExtractor.SIZE_MARKER.matcher("() {01} ").find());
  }

  @Test
  public final void testMultilineUnquotedSubject() throws IOException, ParseException {
    @SuppressWarnings("unchecked")
    final List<MessageStatus> extract =
        new MessageStatusExtractor().extract(IOUtils.readLines(new StringReader(
            "* 4017 FETCH (X-GM-THRID 13603320389284585 X-GM-MSGID 1460332038925224585 X-GM-LABELS (\"\\\\Inbox\") UID 5474 RFC822.SIZE 24864 INTERNALDATE \"10-Feb-2011 04:55:27 +0000\" FLAGS (\\Seen) ENVELOPE (\"Thu, 10 Feb 2011 15:55:20 +1100\" {96}\n" +
                "ASIX News - Social Innovation Sydney Barcamp - Hub Melbourne news - Social\n" +
                "Impact Scholarships ((\"ASIX - Australian Social Innovation eXchange\" NIL \"contact\" \"asix.org.au\")) ((\"ASIX - Australian Social Innovation eXchange\" NIL \"contact\" \"asix.org.au\")) ((\"ASIX - Australian Social Innovation eXchange\" NIL \"contact\" \"asix.org.au\")) ((\"dhanji\" NIL \"mick\" \"rethrick.com\")) NIL NIL NIL \"<E1123Yq-756Z-Fs@c.consumer.fluent.io>\"))\n" +
                "* 4018 FETCH (X-GM-THRID 13603324213584218 X-GM-MSGID 1460332421305584218 X-GM-LABELS (\"\\\\Inbox\") UID 5475 RFC822.SIZE 10992 INTERNALDATE \"10-Feb-2011 05:01:31 +0000\" FLAGS (\\Seen) ENVELOPE (\"Thu, 10 Feb 2011 00:01:31 -0500\" \"Lets do things\" ((\"Mic Nic\" NIL \"thing\" \"fluect.com\")) ((\"Mic Nic\" NIL \"mick\" \"fluent.com\")) ((\"Mic Nic\" NIL \"mic\" \"gmail.com\")) ((\"DJ H\" NIL \"dhanji\" \"gmail.com\")) NIL NIL NIL \"<68432.19238.12837.JavaMail.test@web.fluent.io>\"))"
        )));

    assertNotNull(extract);
    assertEquals(2, extract.size());

    MessageStatus status = extract.get(0);
    assertEquals("\n" +
        "ASIX News - Social Innovation Sydney Barcamp - Hub Melbourne news - Social\n" +
        "Impact Scholarships ", status.getSubject());

    assertEquals("<E1123Yq-756Z-Fs@c.consumer.fluent.io>", status.getMessageUid());
    assertEquals(5474, status.getImapUid());
    assertEquals("\"ASIX - Australian Social Innovation eXchange\" contact@asix.org.au", status.getFrom().get(0));
    assertEquals(ImmutableList.of("\"dhanji\" mick@rethrick.com"), status.getTo());
    assertNull(status.getCc());
    assertNull(status.getBcc());
    assertEquals(ImmutableSet.of(Flag.SEEN), status.getFlags());
    assertEquals(ImmutableSet.of("\"\\\\Inbox\""), status.getLabels());
    assertEquals(1460332038925224585L, (long) status.getGmailMsgId());
    assertEquals(13603320389284585L, (long) status.getThreadId());

    status = extract.get(1);
    assertEquals("Lets do things", status.getSubject());

    assertEquals("<68432.19238.12837.JavaMail.test@web.fluent.io>", status.getMessageUid());
    assertEquals(5475, status.getImapUid());
    assertEquals("\"Mic Nic\" thing@fluect.com", status.getFrom().get(0));
    assertEquals(ImmutableList.of("\"DJ H\" dhanji@gmail.com"), status.getTo());
    assertNull(status.getCc());
    assertNull(status.getBcc());
    assertEquals(ImmutableSet.of(Flag.SEEN), status.getFlags());
    assertEquals(ImmutableSet.of("\"\\\\Inbox\""), status.getLabels());
    assertEquals(1460332421305584218L, (long) status.getGmailMsgId());
    assertEquals(13603324213584218L, (long) status.getThreadId());
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
  //@Test
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
    assertEquals(statuses.size(), 24);
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
