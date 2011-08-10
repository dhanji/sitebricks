package com.google.sitebricks.mail.imap;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;

import static org.testng.Assert.assertEquals;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageStatusExtractorTest {

  @Test
  public final void testTokenizer() throws IOException, ParseException {
    Queue<String> tokens =
        MessageStatusExtractor.tokenize("7 FETCH (ENVELOPE (\"Sun, 10 Apr 2011 16:38:38 +1000\" " +
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
    assertEquals(statuses.size(), 10);
    assertEquals(EnumSet.noneOf(Flag.class), status.getFlags());
    assertEquals("<BANLkTi=zC_UQExUuaNqiP0dJXoswDej1Ww@mail.gmail.com>", status.getMessageUid());
    assertEquals("Get Gmail on your mobile phone", status.getSubject());

    for (int i = 0, statusesSize = statuses.size(); i < statusesSize; i++) {
      MessageStatus st = statuses.get(i);
      assertEquals(st.toString(), assertions.get(i));
    }
  }
}
