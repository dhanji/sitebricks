package com.google.sitebricks.mail.imap;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageStatusExtractorTest {

  @Test
  public final void testTypicalGmailInboxHeaders() throws IOException {
    List<String> lines =
        Resources.readLines(MessageStatusExtractorTest.class.getResource("fetch_all_data.txt"),
            Charsets.UTF_8);

    List<MessageStatus> statuses = new MessageStatusExtractor().extract(lines);
    for (MessageStatus status : statuses) {
      System.out.println(status);
    }
  }

}
