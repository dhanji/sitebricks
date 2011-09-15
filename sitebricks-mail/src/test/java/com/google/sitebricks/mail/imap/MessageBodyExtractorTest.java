package com.google.sitebricks.mail.imap;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageBodyExtractorTest {

  /**
   * WARNING: THIS TEST IS DATA-DEPENDENT!
   */
  @Test
  public final void testTypicalGmailEmail() throws IOException, ParseException {

    List<String> data =
        Resources.readLines(MessageBodyExtractorTest.class.getResource("fetch_body_data1.txt"),
            Charsets.UTF_8);

    List<Message> statuses = new MessageBodyExtractor().extract(data);

    for (int i = 0, statusesSize = statuses.size(); i < statusesSize; i++) {
      Message message = statuses.get(i);
      System.out.println(ToStringBuilder.reflectionToString(message));
      System.out.println("----------->");
      for (Message.BodyPart bodyPart : message.getBodyParts()) {
        System.out.println(ToStringBuilder.reflectionToString(bodyPart));
      }
    }
  }

  @Test
  public final void testReadUnfoldedHeaders() throws IOException {
    URL assertions = MessageBodyExtractorTest.class.getResource("split_headers_assertion_1.txt");
    List<String> data =
        Resources.readLines(
            MessageBodyExtractorTest.class.getResource("split_headers_fetch_data_1.txt"),
            Charsets.UTF_8);

    List<Message> messages = new MessageBodyExtractor().extract(data);
    assertEquals(1, messages.size());
    Message message = messages.get(0);

    // Emit what we've just read back out in a similar format to the file.
    StringBuilder out = new StringBuilder();
    for (Map.Entry<String, Collection<String>> entry : message.getHeaders().asMap().entrySet()) {
      for (String value : entry.getValue()) {
        out.append(entry.getKey())
            .append(": ")
            .append(value)
            .append('\n');
      }
    }

    // Compare the parsed headers with what we slurped in.
    assertEquals(out.toString(), Resources.toString(assertions, Charsets.UTF_8));
  }


  @Test
  public final void testRegex() {
    Pattern pattern = MessageBodyExtractor.MESSAGE_START_REGEX;

    assertTrue(pattern.matcher("* 5 FETCH (BODY[] {2346}").find());
    assertTrue(pattern.matcher("* 235 FETCH (BODY[]").find());
    assertTrue(pattern.matcher("* 1 FETCH (BODY[] AOKSDOAKSD").find());

    assertFalse(pattern.matcher(" * 1 FETCH (BODY[] AOKSDOAKSD").find());
    assertFalse(pattern.matcher("X * 1 FETCH (BODY[] AOKSDOAKSD").find());

    assertFalse(pattern.matcher(" 1 FETCH (BODY[] AOKSDOAKSD").find());
    assertFalse(pattern.matcher("* 1 FETCH(BODY[] AOKSDOAKSD").find());
    assertFalse(pattern.matcher("* 1 FETCH (BODY [ ] AOKSDOAKSD").find());
    assertFalse(pattern.matcher("* 1 FETCH (BODY [] {2345}").find());
    assertFalse(pattern.matcher(" * 1 FETCH (BODY [] {2345}").find());
    assertFalse(pattern.matcher("T * 1 FETCH (BODY [] {2345}").find());
  }
}
