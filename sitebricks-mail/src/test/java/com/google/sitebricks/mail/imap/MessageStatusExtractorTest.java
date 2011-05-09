package com.google.sitebricks.mail.imap;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.EnumSet;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageStatusExtractorTest {

  /**
   * WARNING: THIS TEST IS DATA-DEPENDENT!
   */
  @Test
  public final void testTypicalGmailInboxHeaders() throws IOException, ParseException {
    List<String> lines =
        Resources.readLines(MessageStatusExtractorTest.class.getResource("fetch_all_data.txt"),
            Charsets.UTF_8);

    List<MessageStatus> statuses = new MessageStatusExtractor().extract(lines.subList(0, 1));

    MessageStatus status = statuses.get(0);
    assert EnumSet.noneOf(Flag.class).equals(status.getFlags());
    assert "BANLkTi=zC_UQExUuaNqiP0dJXoswDej1Ww@mail.gmail.com".equals(status.getMessageUid());
    assert new SimpleDateFormat(MessageStatusExtractor.RECEIVED_DATE_FORMAT)
        .parse("Fri, 8 Apr 2011 23:12:09 -0700")
        .equals(status.getReceivedDate());
    assert new SimpleDateFormat(MessageStatusExtractor.INTERNAL_DATE_FORMAT)
        .parse("09-Apr-2011 06:12:09 +0000")
        .equals(status.getInternalDate());
    assert "Get Gmail on your mobile phone".equals(status.getSubject());

    for (MessageStatus st : statuses) {
      System.out.println(st);
    }
  }

}
