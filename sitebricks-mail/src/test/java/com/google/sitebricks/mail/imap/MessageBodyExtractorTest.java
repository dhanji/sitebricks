package com.google.sitebricks.mail.imap;

import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.*;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageBodyExtractorTest {
  private static final Pattern MESSAGE_LOG_REGEX = Pattern.compile("^.* DEBUG c\\.g\\.s\\.mail\\.MailClientHandler: Message received \\[");

  static {
    java.util.logging.ConsoleHandler fh = new java.util.logging.ConsoleHandler();
    java.util.logging.Logger.getLogger("").addHandler(fh);
    java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
  }

//  @Test //DISABLED. Only use this test for debugging.
  public final void testAgainstFewerMessagesParsedThanExistError() throws IOException {
    List<String> data = Resources.readLines(MessageBodyExtractorTest.class.getResource(
        "broken_rfc822.log"), Charsets.UTF_8);

//    List<String> redacted = Lists.newArrayList();
//    for (String line : data) {
//      Matcher matcher = MESSAGE_LOG_REGEX.matcher(line);
//      if (matcher.find()) {
//        line = matcher.replaceAll("");
//        redacted.add(line.substring(0, line.lastIndexOf("]")));
//      }
//    }
//
    List<Message> extract = new MessageBodyExtractor().extract(data);

    for (Message message : extract) {
      Collection<String> messageId = message.getHeaders().get("Message-Id");
      if (messageId.isEmpty())
        messageId = message.getHeaders().get("Message-ID");
      System.out.println(messageId + " "
          + message.getHeaders().get("Subject"));
    }

    System.out.println("Total: " + extract.size());
  }

  /**
   * WARNING: THIS TEST IS DATA-DEPENDENT!
   */
  @Test
  public final void testAwkwardGmailEmailStream() throws IOException, ParseException {
    final List<String> lines =
        Resources.readLines(MessageBodyExtractorTest.class.getResource("fetch_bodies.txt"),
            Charsets.UTF_8);

    List<Message> extract = new MessageBodyExtractor().extract(lines);
    assertEquals(extract.size(), 15);
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
    assertEquals(new String(part1.getBody().getBytes()), new String(IOUtils.toByteArray(
        MessageBodyExtractorTest.class.getResourceAsStream("fetch_body_1_raw.dat"))));

    // ------------------------------------------------------------
    // Second message.
    // missing content-transfer-encoding and mimetype.
    // Should parse it as a UTF-8 text/plain message even though no mimetype is specified,
    // and 7bit CTE.
    message = extract.get(1);
    assertTrue(message.getHeaders().get("Content-Transfer-Encoding").isEmpty());
    assertTrue(message.getHeaders().get("Content-Type").isEmpty());
    assertHeaderEquals(message.getHeaders(), "Subject", "Re: Slow to Respond");

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
        "boundary=\"_000_9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01:morg_\";], " +
        "X-Sitebricks-Test=[multipart-alternatives;quoted-headers]}");

    assertEquals(2, message.getBodyParts().size());
    part1 = message.getBodyParts().get(0);
    Message.BodyPart part2 = message.getBodyParts().get(1);

    assertEquals(1, part1.getHeaders().size());
    assertTrue(Parsing.startsWithIgnoreCase(part1.getHeaders().get("Content-Type").iterator().next(),
        "text/plain"));

    assertEquals(2, part2.getHeaders().size());
    assertTrue(Parsing.startsWithIgnoreCase(part2.getHeaders()
        .get("Content-Type")
        .iterator()
        .next(),
        "text/html"));
    assertEquals(1, part2.getHeaders().get("MIME-Version").size());
    assertHeaderEquals(part2.getHeaders(), "MIME-Version", "1.0");

    assertEquals(part2.getBody(), "<body>\r\n" +
        "I am OOO and may have sporadic access to email.\r\n" +
        "</body>\r\n");

    // ------------------------------------------------------------
    // Fourth message.
    // multipart 2 parts, 1-level deep only, sameline-rparen, preamble, epilogue.
    message = extract.get(3);
    assertEquals(message.getHeaders().toString(),
        "{Delivered-To=[dhanji@gmail.com], Date=[Thu, 8 Sep 2011 17:07:44 -0700]," +
        " Message-ID=[CAEEYBPaoksdpoak+veqCfc+x3m1PxeLn-raisdj@mail.gmail.com]," +
        " Subject=[Re: Slow to Respond], MIME-Version=[1.0], Content-Disposition=[inline]," +
        " Content-Type=[multipart/alternative;" +
        " boundary=_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_]," +
        " X-Sitebricks-Test=[multipart-alternatives;quoted-headers;sameline-rparen;preamble]}");

    assertEquals(2, message.getBodyParts().size());
    part1 = message.getBodyParts().get(0);
    part2 = message.getBodyParts().get(1);

    assertEquals(1, part1.getHeaders().size());
    assertTrue(Parsing.startsWithIgnoreCase(part1.getHeaders().get("Content-Type").iterator().next(),
        "text/plain"));

    assertEquals(part1.getBody(), "I am OOO and may have sporadic access to email.\r\n\r\n" +
        "--\r\n\r\n");

    assertEquals(3, part2.getHeaders().size());
    assertTrue(Parsing.startsWithIgnoreCase(part2.getHeaders()
        .get("Content-Type")
        .iterator()
        .next(),
        "text/html"));
    assertEquals(0, part2.getHeaders().get("MIME-Version").size());
    assertHeaderEquals(part2.getHeaders(), "Content-Disposition", "something");
    assertHeaderEquals(part2.getHeaders(), "Content-Doodle", "somethingelse");

    assertEquals(part2.getBody(), "<body>\r\n" +
        "I am OOO and may have sporadic access to email.\r\n" +
        "</body>\r\n");

    // ------------------------------------------------------------
    // Fifth message.
    // multipart 2 parts, 1-level deep only, tight-preamble/epilogue.
    message = extract.get(4);
    assertEquals(message.getHeaders().toString(),
        "{Delivered-To=[dhanji@gmail.com], Date=[Thu, 8 Sep 2011 17:07:44 -0700]," +
        " Message-ID=[CAEEYBPaoksdpoak+veqCfc+x3m1PxeLn-raisdj@mail.gmail.com]," +
        " Subject=[Re: Slow to Respond], MIME-Version=[1.0], Content-Disposition=[inline]," +
        " Content-Type=[multipart/alternative;" +
        " boundary=_000_9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_]," +
        " X-Sitebricks-Test=[multipart-alternatives;quoted-headers;preamble;epilogue]}");

    assertEquals(2, message.getBodyParts().size());
    part1 = message.getBodyParts().get(0);
    part2 = message.getBodyParts().get(1);

    assertEquals(1, part1.getHeaders().size());
    assertTrue(Parsing.startsWithIgnoreCase(part1.getHeaders().get("Content-Type").iterator().next(),
        "text/plain"));

    assertEquals(part1.getBody(), "I am OOO and may have sporadic access to email.\r\n\r\n" +
        "> OK Success\r\n\r\n" +
        "--\r\n");

    assertEquals(3, part2.getHeaders().size());
    assertTrue(Parsing.startsWithIgnoreCase(part2.getHeaders()
        .get("Content-Type")
        .iterator()
        .next(),
        "text/html"));
    assertEquals(0, part2.getHeaders().get("MIME-Version").size());
    assertHeaderEquals(part2.getHeaders(), "Content-Disposition", "something");
    assertHeaderEquals(part2.getHeaders(), "Content-Doodle", "somethingelse");

    assertEquals(part2.getBody(), "<body>\r\n" +
        "I am OOO and may have sporadic access to email.\r\n" +
        "</body>\r\n");


    // ------------------------------------------------------------
    // Sixth message.
    // multipart 2 parts each, 2-level deep, preambles/epilogues.
    message = extract.get(5);
    assertNestedMultipart2LevelDeep(message, "<CAEEYBPNq6o3M+aisjd+x3m1PxeLn-raisdj@mail.gmail.co" +
        "m>",
        "_000_9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_");

    // ------------------------------------------------------------
    // Seventh message.
    // same as sixth but with wide spacing and different ID.
    message = extract.get(6);
    assertNestedMultipart2LevelDeep(message, "<CAEEYBPNq6o3Mm1PxeLn-raisdj@mail.gmail.com>",
        "_000_9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_");

    // ------------------------------------------------------------
    // Eigth message.
    // same as sixth but with compact spacing and different ID.
    message = extract.get(7);
    assertNestedMultipart2LevelDeep(message, "<SPLAT_CAEEYBPNq6o3Mm1PxeLn-raisdj@mail.gmail.com>",
        "_000_9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_");

    // ------------------------------------------------------------
    // Ninth message.
    // same as eigth but re-using the SAME boundary for inner and outer parts.
    message = extract.get(8);
    assertNestedMultipart2LevelDeep(message, "<SPLAT_CAEEYBPNq6o3Mm1PxeLn-raisdj@mail.gmail.com>",
        "----NextPart_1293809.9123_LLas");

    // ------------------------------------------------------------
    // Tenth message.
    // multipart 3 parts each, 3-level deep, preambles/epilogues.
    message = extract.get(9);
    assertComplexNestedStructure(message);

    // ------------------------------------------------------------
    // Eleventh message.
    // multipart 3 parts, 1-level deep, message/rfc822 nested message with quoted-printable.
    message = extract.get(10);
    assertRfc822(message, "quoted-printable");

    // ------------------------------------------------------------
    // Twelfth message.
    // multipart 3 parts, 1-level deep, message/rfc822 nested message.
    message = extract.get(11);
    assertRfc822(message, null);

    // ------------------------------------------------------------
    // Thirteenth message.
    // multipart 3 parts, message/rfc822 nested multipart message.
    message = extract.get(12);
    assertMultipartRfc822(message);

    // ------------------------------------------------------------
    // Fourteenth message.
    // multipart 3 parts, message/rfc822 nested message with multipart and attachment.
    message = extract.get(13);
    assertRfc822withAttachment(message);

    // ------------------------------------------------------------
    // Fourteenth message.
    // Test mixed case in Content-Type.
    message = extract.get(14);
    assertEquals(2, message.getBodyParts().size());
  }

  private void assertRfc822(Message message, String contentTransferEncoding) {
    assertEquals(3, message.getBodyParts().size());

    Message.BodyPart part1;
    Message.BodyPart part2;
    Message.BodyPart part3;

    part1 = message.getBodyParts().get(0);
    part2 = message.getBodyParts().get(1);
    part3 = message.getBodyParts().get(2);

    assertNotNull(part1);
    assertNotNull(part2);
    assertNotNull(part3);

    assertTrue(Parsing.startsWithIgnoreCase(part1.getHeaders().get("Content-Type").iterator().next(), "text/plain"));
    assertTrue(Parsing.startsWithIgnoreCase(part3.getHeaders().get("Content-Type").iterator().next(), "text/plain"));

    // Message 2 is an encapsulated rfc822 message.
    assertTrue(
        Parsing.startsWithIgnoreCase(part2.getHeaders().get("Content-Type").iterator().next(),
            "message/rfc822"));
    if (contentTransferEncoding != null)
      assertTrue(
          Parsing.startsWithIgnoreCase(part2.getHeaders().get("Content-Transfer-Encoding").iterator().next(),
              contentTransferEncoding));

    assertNull(part2.getBody());
    assertNull(part2.getBinBody());

    assertEquals(1, part2.getBodyParts().size());
    // It should contain its content as a child message.
    Message.BodyPart rfc822 = part2.getBodyParts().get(0);

    assertNotNull(rfc822);
    assertEquals(rfc822.getHeaders().size(), 7);

    assertEquals(rfc822.getHeaders().get("Message-ID").iterator().next(), "<9632091.970.1320441146867.JavaMail.geo-discussion-forums@yqie15>");
    assertEquals(rfc822.getHeaders().get("In-Reply-To").iterator().next(), "<AANLkTikNOzOVjj=mS8nFXoiuW=LPufKKsK_SOPEXdCby@mail.gmail.com>");
    assertEquals(rfc822.getHeaders().get("X-Annoy").iterator().next(), "dhanji");
    assertEquals(rfc822.getHeaders().get("From").iterator().next(), "example@example.com");
    assertEquals(rfc822.getHeaders().get("To").iterator().next(), "example2@example.com");
    assertEquals(rfc822.getHeaders().get("Subject").iterator().next(), "As basic as it gets");
    assertEquals(rfc822.getHeaders().get("Content-Type").iterator().next(), "text/plain");

    assertNull(rfc822.getBinBody());
    assertNotNull(rfc822.getBody());
    assertEquals("This is the plain text body of the message.  Note the blank line\r\n" +
        "between the header information and the body of the message.\r\n\r\n", rfc822.getBody());
  }

  private void assertMultipartRfc822(Message message) {
    // Assume all the stuff about the non-rfc822 matches the previous case.
    // skip right down the the nested message.
    assertEquals(3, message.getBodyParts().size());

    Message.BodyPart part2;

    part2 = message.getBodyParts().get(1);
    assertNotNull(part2);

    // Message 2 is an encapsulated rfc822 message.
    assertTrue(
        Parsing.startsWithIgnoreCase(part2.getHeaders().get("Content-Type").iterator().next(),
            "message/rfc822"));

    assertNull(part2.getBody());
    assertNull(part2.getBinBody());

    assertEquals(1, part2.getBodyParts().size());
    // It should contain its content as a child message.
    Message.BodyPart rfc822 = part2.getBodyParts().get(0);

    assertNotNull(rfc822);
    assertEquals(rfc822.getHeaders().size(), 7);

    assertEquals(rfc822.getHeaders().get("Message-ID").iterator().next(), "<9632091.970.1320441146867.JavaMail.geo-discussion-forums@yqie15>");
    assertEquals(rfc822.getHeaders().get("In-Reply-To").iterator().next(), "<AANLkTikNOzOVjj=mS8nFXoiuW=LPufKKsK_SOPEXdCby@mail.gmail.com>");
    assertEquals(rfc822.getHeaders().get("X-Annoy").iterator().next(), "dhanji");
    assertEquals(rfc822.getHeaders().get("From").iterator().next(), "example@example.com");
    assertEquals(rfc822.getHeaders().get("To").iterator().next(), "example2@example.com");
    assertEquals(rfc822.getHeaders().get("Subject").iterator().next(), "As basic as it gets");
    assertEquals(rfc822.getHeaders().get("Content-Type").iterator().next(), "multipart/mixed; boundary=e89a8ff1c384d8017504b42beb91");

    assertEquals(2, rfc822.getBodyParts().size());

    Message.BodyPart sub1 = rfc822.getBodyParts().get(0);
    Message.BodyPart sub2 = rfc822.getBodyParts().get(1);

    assertEquals(sub1.getHeaders().get("Content-Type").iterator().next(), "text/plain; charset=ISO-8859-1");
    assertNotNull(sub1.getBody());
    assertNull(sub1.getBinBody());
    assertEquals(sub2.getHeaders().get("Content-Type").iterator().next(), "text/plain; charset=ISO-8859-1");
    assertNotNull(sub2.getBody());
    assertNull(sub2.getBinBody());
   }


  private void assertRfc822withAttachment(Message message) {
    // Assume all the stuff about the non-rfc822 matches the previous case.
    // skip right down the the nested message.
    assertEquals(message.getBodyParts().size(), 3);

    Message.BodyPart part2;

    part2 = message.getBodyParts().get(1);
    assertNotNull(part2);

    // Message 2 is an encapsulated rfc822 message.
    assertTrue(
        Parsing.startsWithIgnoreCase(part2.getHeaders().get("Content-Type").iterator().next(),
            "message/rfc822"));
    assertTrue(
        Parsing.startsWithIgnoreCase(part2.getHeaders().get("Content-Transfer-Encoding").iterator().next(),
            "quoted-printable"));

    assertNull(part2.getBody());
    assertNull(part2.getBinBody());

    assertEquals(1, part2.getBodyParts().size());
    // It should contain its content as a child message.
    Message.BodyPart rfc822 = part2.getBodyParts().get(0);

    assertNotNull(rfc822);
    assertEquals(rfc822.getHeaders().size(), 7);

    assertEquals(rfc822.getHeaders().get("Message-ID").iterator().next(), "<9632091.970.1320441146867.JavaMail.geo-discussion-forums@yqie15>");
    assertEquals(rfc822.getHeaders().get("In-Reply-To").iterator().next(), "<AANLkTikNOzOVjj=mS8nFXoiuW=LPufKKsK_SOPEXdCby@mail.gmail.com>");
    assertEquals(rfc822.getHeaders().get("X-Annoy").iterator().next(), "dhanji");
    assertEquals(rfc822.getHeaders().get("From").iterator().next(), "example@example.com");
    assertEquals(rfc822.getHeaders().get("To").iterator().next(), "example2@example.com");
    assertEquals(rfc822.getHeaders().get("Subject").iterator().next(), "As basic as it gets");
    assertEquals(rfc822.getHeaders().get("Content-Type").iterator().next(), "multipart/mixed; boundary=e89a8ff1c384d8017504b42beb91");

    assertEquals(2, rfc822.getBodyParts().size());

    Message.BodyPart sub1 = rfc822.getBodyParts().get(0);
    Message.BodyPart sub2 = rfc822.getBodyParts().get(1);

    assertEquals(sub1.getHeaders().get("Content-Type").iterator().next(), "text/plain; charset=ISO-8859-1");
    assertNotNull(sub1.getBody());
    assertNull(sub1.getBinBody());
    assertEquals(sub2.getHeaders().get("Content-Type").iterator().next(), "text/csv; charset=US-ASCII; name=\"csv-demo.csv\"");
    assertNull(sub2.getBody());
    assertNotNull(sub2.getBinBody());
   }

  private void assertComplexNestedStructure(Message message) {
    Message.BodyPart part1;
    Message.BodyPart part2;
    Message.BodyPart part3;

    Message.BodyPart innerPart1;
    assertEquals(message.getHeaders().toString(),
        "{Delivered-To=[dhanji@gmail.com], Message-ID=[<id> id]," +
        " Subject=[Re: Slow to Respond], Content-Type=[multipart/alternative;" +
            " boundary = __BOUNDARY__]," +
        " X-Sitebricks-Test=[multipart-alternatives;quoted-headers;nested-parts;preamble]}");

    assertEquals(3, message.getBodyParts().size());
    part1 = message.getBodyParts().get(0);
    part2 = message.getBodyParts().get(1);
    part3 = message.getBodyParts().get(2);

    assertEquals(1, part1.getBodyParts().size());

    innerPart1 = part1.getBodyParts().get(0);
    assertEquals(2, innerPart1.getBodyParts().size());

    Message.BodyPart innerInnerPart1 = innerPart1.getBodyParts().get(0);
    assertEquals(3, innerInnerPart1.getBodyParts().size());

    Message.BodyPart innerInnerPart2 = innerPart1.getBodyParts().get(1);
    assertEquals("Hi this is a body.\r\n\r\n", innerInnerPart2.getBody());

    // Back to TOP level.
    assertEquals("This is a signature.\r\n", part2.getBody());

    // Last top-level part.
    assertEquals(3, part3.getBodyParts().size());

    assertEquals("This is a signature.\r\n" +
        "--__BOUNDARY-2__\r\n" +
        "--__BOUNDARY-2__--\r\n" +
        "--__BOUNDARY-1__--\r\n" +
        "fooled you--this is all textbody.\r\n\r\n", part3.getBodyParts().get(0).getBody());

    assertEquals("Beric is dead.\r\n", part3.getBodyParts().get(1).getBody());

    Message.BodyPart peric = part3.getBodyParts().get(2);
    assertEquals(2, peric.getBodyParts().size());

    assertEquals(peric.getBodyParts().get(0).getBody(), "HI!\r\n\r\n");
    assertHeaderEquals(peric.getBodyParts().get(0).getHeaders(), "Content-Type", "text/plain");

    assertEquals(peric.getBodyParts().get(1).getBody(), "<body>yo</body>\r\n\r\n");
    assertHeaderEquals(peric.getBodyParts().get(1).getHeaders(), "Content-Type", "text/html");
  }

  private void assertNestedMultipart2LevelDeep(Message message,
                                               String id, String boundary) {
    Message.BodyPart part1;
    Message.BodyPart part2;
    assertEquals(message.getHeaders().toString(),
        "{Delivered-To=[dhanji@gmail.com], Message-ID=[" + id + "]," +
        " Subject=[Re: Slow to Respond], Content-Type=[multipart/alternative;" +
            " boundary=" + boundary + "]," +
        " X-Sitebricks-Test=[multipart-alternatives;quoted-headers;nested-parts;preamble]}");

    assertEquals(2, message.getBodyParts().size());
    part1 = message.getBodyParts().get(0);
    part2 = message.getBodyParts().get(1);

    // The first part should itself have two parts.
    assertEquals(2, part1.getBodyParts().size());
    Message.BodyPart innerPart1 = part1.getBodyParts().get(0);
    Message.BodyPart innerPart2 = part1.getBodyParts().get(1);

    assertEquals(1, part1.getHeaders().size());
    assertTrue(
        Parsing.startsWithIgnoreCase(part1.getHeaders().get("Content-Type").iterator().next(),
            "multipart/alternative"));

    // Inner parts should be as exepcted.
    assertTrue(Parsing.startsWithIgnoreCase(innerPart1.getHeaders().get("Content-Type").iterator().next(),
            "text/plain"));
    assertTrue(Parsing.startsWithIgnoreCase(innerPart2.getHeaders().get("Content-Type").iterator().next(),
            "text/html"));
    assertEquals(innerPart1.getBody(), "I am OOO and may have sporadic access to email.\r\n\r\n");
    assertEquals(innerPart2.getBody(), "<body>\r\n" +
        "I am OOO and may have sporadic access to email.\r\n" +
        "</body>\r\n\r\n");


    // The multipart body part itself has no body, instead has subparts.
    assertNull(part1.getBody());

    assertEquals(2, part2.getHeaders().size());
    assertTrue(Parsing.startsWithIgnoreCase(part2.getHeaders()
        .get("Content-Type")
        .iterator()
        .next(),
        "text/plain"));
    assertHeaderEquals(part2.getHeaders(), "MIME-Version", "1.0");
    assertEquals(part2.getBody(), "This is a signature.\r\n\r\n");
  }

  private static void assertHeaderEquals(Multimap<String, String> headers, String header, String value) {
    assertEquals(headers.get(header).iterator().next(), value);
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
    Pattern pattern = MessageBodyExtractor.MESSAGE_START_PREFIX_REGEX;

    assertTrue(pattern.matcher("* 5 FETCH (UID 1001 BODY[] {2346}").find());
    assertTrue(pattern.matcher("* 235 FETCH (UID 1001 BODY[]").find());
    assertTrue(pattern.matcher("* 1 FETCH (UID 1001 BODY[] AOKSDOAKSD").find());

    assertFalse(pattern.matcher(" * 1 FETCH (UID 1001 BODY[] AOKSDOAKSD").find());
    assertFalse(pattern.matcher("X * 1 FETCH (UID 1001 BODY[] AOKSDOAKSD").find());

    assertFalse(pattern.matcher(" 1 FETCH (UID1001 BODY[] AOKSDOAKSD").find());
    assertFalse(pattern.matcher("* 1 FETCH(UID 1001  BODY [] AOKSDOAKSD").find());
    assertFalse(pattern.matcher("* 1 FETCH(UID 1001BODY [] AOKSDOAKSD").find());
    assertFalse(pattern.matcher(" 1 FETCH (UID 1001 BODY[] AOKSDOAKSD").find());
    assertFalse(pattern.matcher("* 1 FETCH(UID 1001 BODY[] AOKSDOAKSD").find());
    assertFalse(pattern.matcher("* 1 FETCH (UID 1001 BODY [ ] AOKSDOAKSD").find());
    assertFalse(pattern.matcher("* 1 FETCH (UID 1001 BODY [] {2345}").find());
    assertFalse(pattern.matcher(" * 1 FETCH (UID 1001 BODY [] {2345}").find());
    assertFalse(pattern.matcher("T * 1 FETCH (UID 1001 BODY [] {2345}").find());
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

  @Test
  public final void testBoundaryExtractorRegex() {
    Matcher matcher = MessageBodyExtractor.BOUNDARY_REGEX.matcher(
        "multipart/alternative;\n" +
            " boundary=_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_");
    assertTrue(matcher.find());
    assertEquals("_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_", matcher.group(1));

    matcher = MessageBodyExtractor.BOUNDARY_REGEX.matcher(
        "multipart/alternative;\n" +
            " boundary=\"_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_\"");
    assertTrue(matcher.find());
    assertEquals("_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_", matcher.group(1));

    matcher = MessageBodyExtractor.BOUNDARY_REGEX.matcher(
        "multipart/alternative;\n" +
            " boundary =\"_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_\"");
    assertTrue(matcher.find());
    assertEquals("_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_", matcher.group(1));

    matcher = MessageBodyExtractor.BOUNDARY_REGEX.matcher(
        "multipart/alternative;\n" +
            " boundary = \"_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_\";");
    assertTrue(matcher.find());
    assertEquals("_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_", matcher.group(1));

    matcher = MessageBodyExtractor.BOUNDARY_REGEX.matcher(
        "multipart/alternative;\n" +
            " boundary =     \"_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_");
    assertTrue(matcher.find());
    assertEquals("_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_", matcher.group(1));

    matcher = MessageBodyExtractor.BOUNDARY_REGEX.matcher(
        "multipart/alternative;" +
            "boundary =     \"_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_");
    assertTrue(matcher.find());
    assertEquals("_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_", matcher.group(1));


    // Boundary function
    assertEquals("--_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_",
        MessageBodyExtractor.boundary("multipart/alternative;" +
            "boundary =     _000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_  "));
    assertEquals("--_000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_",
        MessageBodyExtractor.boundary("multipart/alternative;" +
            "BOUNDARY =     _000_:9E22DB2E4EF0164D9F76BB4BC3FC689E31BCF27D87CPPXCMS01morg_  "));

    // Invalid values... (spam?)
    assertNull(MessageBodyExtractor.boundary("multipart/alternative;" +
            "boundary ="));
    assertNull(MessageBodyExtractor.boundary("multipart/alternative;"));
    assertNull(MessageBodyExtractor.boundary("multipart/alternative;;;"));
  }

  @Test
  public final void testCharsetExtractorRegex() {
    // Charset function
    assertEquals("us-ascii", MessageBodyExtractor.charset("text/html;\n" +
        " charset=us-ascii"));
    assertEquals("us-ascii", MessageBodyExtractor.charset("text/html;\n" +
            " charset=us-ascii  "));
    assertEquals("us-ascii", MessageBodyExtractor.charset("text/html;\n" +
            " charset=\"us-ascii\""));
    assertEquals("us-ascii", MessageBodyExtractor.charset("text/html;\n" +
            " charset = \"us-ascii\""));
    assertEquals("us-ascii", MessageBodyExtractor.charset("text/html;\n" +
            " charset=\"us-ascii  \""));
    assertEquals("us-ascii", MessageBodyExtractor.charset("text/html;\n" +
            " CHARSET =\"us-ascii  \""));
    assertEquals("US-ASCII", MessageBodyExtractor.charset("text/html;\n" +
            " CHARSET =\"US-ASCII  \""));

    assertEquals("UTF-8", MessageBodyExtractor.charset("text/html;\n" +
            " charset="));
    assertEquals("UTF-8", MessageBodyExtractor.charset("text/html;\n" +
            " CHARSET="));
    assertEquals("UTF-8", MessageBodyExtractor.charset("text/html"));
    assertEquals("UTF-8", MessageBodyExtractor.charset("text/html;;;"));
    assertEquals("UTF-8", MessageBodyExtractor.charset("text/html;charset=;;"));
    assertEquals("UTF-8", MessageBodyExtractor.charset(""));
    assertEquals("UTF-8", MessageBodyExtractor.charset(null));
  }
}
