package com.google.sitebricks.mail.imap;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.io.IOUtils;
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
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageBodyExtractorTest {
  private static final Pattern MESSAGE_LOG_REGEX = Pattern.compile("^.* DEBUG c\\.g\\.s\\.mail\\.MailClientHandler: Message received \\[");

  /**
   * WARNING: THIS TEST IS DATA-DEPENDENT!
   */
  @Test
  public final void testAwkwardGmailEmailStream() throws IOException, ParseException {
    final List<String> lines =
        Resources.readLines(MessageBodyExtractorTest.class.getResource("fetch_bodies.txt"),
            Charsets.UTF_8);

    List<Message> extract = new MessageBodyExtractor().extract(lines);

    // ------------------------------------------------------------
    // First message.
    // Folded headers with tabs + spaces, repeat headers, one body.
    Message message = extract.get(0);
    String expectedHeaders =
        IOUtils.toString(MessageBodyExtractorTest.class.getResourceAsStream("fetch_headers_1.txt"));
    assertEquals(message.getHeaders().toString(), expectedHeaders);

    assertEquals(1, message.getBodyParts().size());
    Message.BodyPart part1 = message.getBodyParts().get(0);
    assertNull(part1.getBinBody());
    assertTrue(part1.getHeaders().isEmpty());

    // We have to compare the raw bytes because the encoded string comes in as ISO-8859-1
    // And Java literals are encoded as UTF-8.
    assertEquals(part1.getBody().getBytes(), IOUtils.toByteArray(
        MessageBodyExtractorTest.class.getResourceAsStream("fetch_body_1_raw.dat")));


    // ------------------------------------------------------------
    // Second message.
    // missing content-transfer-encoding and mimetype.
    // Should parse it as a UTF-8 text/plain message even though no mimetype is specified,
    // and 7bit CTE.
    message = extract.get(1);
    assertTrue(message.getHeaders().get("Content-Transfer-Encoding").isEmpty());
    assertTrue(message.getHeaders().get("Content-Type").isEmpty());
    assertEquals(message.getHeaders().get("Subject").iterator().next(), "Re: Slow to Respond");

    assertEquals(1, message.getBodyParts().size());
    part1 = message.getBodyParts().get(0);
    assertTrue(part1.getHeaders().isEmpty());
    assertNull(part1.getBinBody());
    assertEquals(part1.getBody(), IOUtils.toString(
        MessageBodyExtractorTest.class.getResourceAsStream("fetch_body_2.txt")));

    // ------------------------------------------------------------
    // Third message.
    // multipart 2 parts, 1-level deep only.
    message = extract.get(2);
    assertEquals(message.getHeaders().toString(),
        "{Message-ID=[<askdopaksdNq6o3M+veqCfc+x3m1PxeLn-raisdj" +
        "@mail.gmail.com>], Subject=[Re: Slow to Respond], Content-Type=[multipart/alternative; " +
        "boundary=\"_000_9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_\"], " +
        "X-Sitebricks-Test=[multipart-alternatives;quoted-headers]}");

    assertEquals(2, message.getBodyParts().size());
    part1 = message.getBodyParts().get(0);
    Message.BodyPart part2 = message.getBodyParts().get(1);

    assertTrue(Parsing.startsWithIgnoreCase(part1.getHeaders().get("Content-Type").iterator().next(),
        "text/plain"));
    System.out.println(part2.getHeaders());
//    assertTrue(Parsing.startsWithIgnoreCase(part2.getHeaders().get("Content-Type").iterator().next(),
//        "text/html"));

    assertEquals(part2.getBody(), "<body>\r\n" +
        "I am OOO and may have sporadic access to email.\r\n" +
        "</body>\r\n");
  }

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
  public final void testStartRegex() {
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

  @Test
  public final void testEosRegex() {
    Pattern pattern = MessageBodyExtractor.EOS_REGEX;

    assertTrue(pattern.matcher("4 OK SUCCESS").matches());
    assertTrue(pattern.matcher("5 OK SUCCESS").matches());
    assertTrue(pattern.matcher("22 ok success").matches());

    assertFalse(pattern.matcher(") (").matches());
    assertFalse(pattern.matcher("(").matches());
  }
}
