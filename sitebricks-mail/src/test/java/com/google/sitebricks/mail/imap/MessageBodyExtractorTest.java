package com.google.sitebricks.mail.imap;

import com.google.common.base.Charsets;
import com.google.common.collect.Multimap;
import com.google.common.io.Resources;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.testng.annotations.Test;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
  public final void testAwkwardGmailEmailStreamUsingTruncatorGroping() throws IOException, ParseException {
    testAwkwardGmailEmailStream(true);
  }

  /**
   * WARNING: THIS TEST IS DATA-DEPENDENT!
   */
  @Test
  public final void testAwkwardGmailEmailStreamUsingLengths() throws IOException, ParseException {
    testAwkwardGmailEmailStream(false);
  }


  public final void testAwkwardGmailEmailStream(boolean forceTruncatorGroping) throws IOException, ParseException {
    final List<String> lines =
        Resources.readLines(MessageBodyExtractorTest.class.getResource("fetch_bodies.txt"),
            Charsets.UTF_8);

    List<Message> extract = new MessageBodyExtractor(forceTruncatorGroping, 999999999999999999L).extract(lines);
    assertEquals(extract.size(), 21);
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
    // multipart 3 parts, 1-level deep, message/rfc822 nested message.
    message = extract.get(11);
    assertRfc822(message, null);

    // ------------------------------------------------------------
    // multipart 3 parts, message/rfc822 nested multipart message.
    message = extract.get(12);
    assertMultipartRfc822(message);

    // ------------------------------------------------------------
    // multipart 3 parts, message/rfc822 nested message with multipart and attachment.
    message = extract.get(13);
    assertRfc822withAttachment(message);

    // ------------------------------------------------------------
    // Test mixed case in Content-Type.
    message = extract.get(14);
    assertEquals(2, message.getBodyParts().size());

    // ------------------------------------------------------------
    // Test mixed case in Content-Type.
    message = extract.get(15);
    assertEquals(1, message.getBodyParts().size());
    assertEquals(message.getBodyParts().get(0).getBody(),
        "Danke für die Weihnachtswünsche! Viele Grüße.\r\n");

    // ------------------------------------------------------------
    // This one is intentionally broken and forces terminator groping,
    // check that we get what we expect.
    message = extract.get(16);
    assertEquals(1, message.getBodyParts().size());
    assertEquals(message.getBodyParts().get(0).getBody(),
        "the message body\r\n)\r\n\r\n45988 OK Success\r\n");

    message = extract.get(17);
    assertEquals(1, message.getBodyParts().size());
    if (forceTruncatorGroping)
      assertEquals(message.getBodyParts().get(0).getBody(),
          "fake ending\r\n\r\n");
    else
      assertEquals(message.getBodyParts().get(0).getBody(),
          "fake ending\r\n\r\n)\r\n10 OK Success\r\n");

    // ------------------------------------------------------------
    // Many parts, with verified length as sent by gmail.
    message = extract.get(18);
    assertEquals(4, message.getBodyParts().size());

    // ------------------------------------------------------------
    // Invalid body length, but still expect correct parsing
    message = extract.get(19);
    assertEquals(1, message.getBodyParts().size());
    assertEquals(message.getBodyParts().get(0).getBody(),
          "the message body\r\n");

    message = extract.get(20);
    assertEquals(1, message.getBodyParts().size());
    assertEquals(message.getBodyParts().get(0).getBody(),
        "the message body\r\n");
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

  @Test
  public final void testDecoding() throws MessagingException, IOException {
    String body = "Grüße";
    String encoding = "8bit";
    String charset = "ISO-8859-1";
    final byte[] bytes = body.getBytes(charset);
    final InputStream decoded = MimeUtility.decode(new ByteArrayInputStream(bytes), encoding);
    String result = IOUtils.toString(decoded, charset);
    assertEquals(result, body);
  }

  @Test
  public void testBase64() throws Exception {
    String sample =
    "PGh0bWwgeG1sbnM6dj0idXJuOnNjaGVtYXMtbWljcm9zb2Z0LWNvbTp2bWwiIHhtbG5zOm89" +
    "InVybjpzY2hlbWFzLW1pY3Jvc29mdC1jb206b2ZmaWNlOm9mZmljZSIgeG1sbnM6dz0idXJu" +
    "OnNjaGVtYXMtbWljcm9zb2Z0LWNvbTpvZmZpY2U6d29yZCIgeG1sbnM6bT0iaHR0cDovL3Nj" +
    "aGVtYXMubWljcm9zb2Z0LmNvbS9vZmZpY2UvMjAwNC8xMi9vbW1sIiB4bWxucz0iaHR0cDov" +
    "L3d3dy53My5vcmcvVFIvUkVDLWh0bWw0MCI+PGhlYWQ+PG1ldGEgaHR0cC1lcXVpdj1Db250" +
    "ZW50LVR5cGUgY29udGVudD0idGV4dC9odG1sOyBjaGFyc2V0PXV0Zi04Ij48bWV0YSBuYW1l" +
    "PUdlbmVyYXRvciBjb250ZW50PSJNaWNyb3NvZnQgV29yZCAxMiAoZmlsdGVyZWQgbWVkaXVt" +
    "KSI+PHN0eWxlPjwhLS0NCi8qIEZvbnQgRGVmaW5pdGlvbnMgKi8NCkBmb250LWZhY2UNCgl7" +
    "Zm9udC1mYW1pbHk6Q2FsaWJyaTsNCglwYW5vc2UtMToyIDE1IDUgMiAyIDIgNCAzIDIgNDt9" +
    "DQpAZm9udC1mYWNlDQoJe2ZvbnQtZmFtaWx5OlRhaG9tYTsNCglwYW5vc2UtMToyIDExIDYg" +
    "NCAzIDUgNCA0IDIgNDt9DQovKiBTdHlsZSBEZWZpbml0aW9ucyAqLw0KcC5Nc29Ob3JtYWws" +
    "IGxpLk1zb05vcm1hbCwgZGl2Lk1zb05vcm1hbA0KCXttYXJnaW46MGNtOw0KCW1hcmdpbi1i" +
    "b3R0b206LjAwMDFwdDsNCglmb250LXNpemU6MTIuMHB0Ow0KCWZvbnQtZmFtaWx5OiJUaW1l" +
    "cyBOZXcgUm9tYW4iLCJzZXJpZiI7fQ0KYTpsaW5rLCBzcGFuLk1zb0h5cGVybGluaw0KCXtt" +
    "c28tc3R5bGUtcHJpb3JpdHk6OTk7DQoJY29sb3I6Ymx1ZTsNCgl0ZXh0LWRlY29yYXRpb246" +
    "dW5kZXJsaW5lO30NCmE6dmlzaXRlZCwgc3Bhbi5Nc29IeXBlcmxpbmtGb2xsb3dlZA0KCXtt" +
    "c28tc3R5bGUtcHJpb3JpdHk6OTk7DQoJY29sb3I6cHVycGxlOw0KCXRleHQtZGVjb3JhdGlv" +
    "bjp1bmRlcmxpbmU7fQ0Kc3Bhbi5FbWFpbFN0eWxlMTcNCgl7bXNvLXN0eWxlLXR5cGU6cGVy" +
    "c29uYWwtcmVwbHk7DQoJZm9udC1mYW1pbHk6IkNhbGlicmkiLCJzYW5zLXNlcmlmIjsNCglj" +
    "b2xvcjojMUY0OTdEO30NCi5Nc29DaHBEZWZhdWx0DQoJe21zby1zdHlsZS10eXBlOmV4cG9y" +
    "dC1vbmx5Ow0KCWZvbnQtc2l6ZToxMC4wcHQ7fQ0KQHBhZ2UgV29yZFNlY3Rpb24xDQoJe3Np" +
    "emU6NjEyLjBwdCA3OTIuMHB0Ow0KCW1hcmdpbjo3Mi4wcHQgNzIuMHB0IDcyLjBwdCA3Mi4w" +
    "cHQ7fQ0KZGl2LldvcmRTZWN0aW9uMQ0KCXtwYWdlOldvcmRTZWN0aW9uMTt9DQotLT48L3N0" +
    "eWxlPjwhLS1baWYgZ3RlIG1zbyA5XT48eG1sPg0KPG86c2hhcGVkZWZhdWx0cyB2OmV4dD0i" +
    "ZWRpdCIgc3BpZG1heD0iMTAyNiIgLz4NCjwveG1sPjwhW2VuZGlmXS0tPjwhLS1baWYgZ3Rl" +
    "IG1zbyA5XT48eG1sPg0KPG86c2hhcGVsYXlvdXQgdjpleHQ9ImVkaXQiPg0KPG86aWRtYXAg" +
    "djpleHQ9ImVkaXQiIGRhdGE9IjEiIC8+DQo8L286c2hhcGVsYXlvdXQ+PC94bWw+PCFbZW5k" +
    "aWZdLS0+PC9oZWFkPjxib2R5IGxhbmc9RU4tQVUgbGluaz1ibHVlIHZsaW5rPXB1cnBsZT48" +
    "ZGl2IGNsYXNzPVdvcmRTZWN0aW9uMT48cCBjbGFzcz1Nc29Ob3JtYWw+PHNwYW4gc3R5bGU9" +
    "J2ZvbnQtc2l6ZToxMS4wcHQ7Zm9udC1mYW1pbHk6IkNhbGlicmkiLCJzYW5zLXNlcmlmIjtj" +
    "b2xvcjojMUY0OTdEJz5IZXkgQ2FtZXJvbiw8bzpwPjwvbzpwPjwvc3Bhbj48L3A+PHAgY2xh" +
    "c3M9TXNvTm9ybWFsPjxzcGFuIHN0eWxlPSdmb250LXNpemU6MTEuMHB0O2ZvbnQtZmFtaWx5" +
    "OiJDYWxpYnJpIiwic2Fucy1zZXJpZiI7Y29sb3I6IzFGNDk3RCc+PG86cD4mbmJzcDs8L286" +
    "cD48L3NwYW4+PC9wPjxwIGNsYXNzPU1zb05vcm1hbD48c3BhbiBzdHlsZT0nZm9udC1zaXpl" +
    "OjExLjBwdDtmb250LWZhbWlseToiQ2FsaWJyaSIsInNhbnMtc2VyaWYiO2NvbG9yOiMxRjQ5" +
    "N0QnPkkgaGVsZCBvZmYgbGFzdCB0aW1lIHdlIHNwb2tlIGJ1dCBpdOKAmXMgYWxtb3N0IEZl" +
    "YnJ1YXJ5IDE0LiBJ4oCZZCBsaWtlIHRvIHB1Ymxpc2ggc29tZXRoaW5nIG5leHQgd2Vlay4g" +
    "wqBDYW4gSSBnZXQgYW4gaW52aXRhdGlvbiB0byB0aGUgc2VydmljZT88bzpwPjwvbzpwPjwv" +
    "c3Bhbj48L3A+PHAgY2xhc3M9TXNvTm9ybWFsPjxzcGFuIHN0eWxlPSdmb250LXNpemU6MTEu" +
    "MHB0O2ZvbnQtZmFtaWx5OiJDYWxpYnJpIiwic2Fucy1zZXJpZiI7Y29sb3I6IzFGNDk3RCc+" +
    "PG86cD4mbmJzcDs8L286cD48L3NwYW4+PC9wPjxwIGNsYXNzPU1zb05vcm1hbD48c3BhbiBz" +
    "dHlsZT0nZm9udC1zaXplOjExLjBwdDtmb250LWZhbWlseToiQ2FsaWJyaSIsInNhbnMtc2Vy" +
    "aWYiO2NvbG9yOiMxRjQ5N0QnPkFyZSB0aGVyZSBwaWN0dXJlcyBvZiB5b3UgZ3V5cyB0b2dl" +
    "dGhlciBJIGNhbiB1c2U/PG86cD48L286cD48L3NwYW4+PC9wPjxwIGNsYXNzPU1zb05vcm1h" +
    "bD48c3BhbiBzdHlsZT0nZm9udC1zaXplOjExLjBwdDtmb250LWZhbWlseToiQ2FsaWJyaSIs" +
    "InNhbnMtc2VyaWYiO2NvbG9yOiMxRjQ5N0QnPjxvOnA+Jm5ic3A7PC9vOnA+PC9zcGFuPjwv" +
    "cD48ZGl2IHN0eWxlPSdib3JkZXI6bm9uZTtib3JkZXItdG9wOnNvbGlkICNCNUM0REYgMS4w" +
    "cHQ7cGFkZGluZzozLjBwdCAwY20gMGNtIDBjbSc+PHAgY2xhc3M9TXNvTm9ybWFsPjxiPjxz" +
    "cGFuIGxhbmc9RU4tVVMgc3R5bGU9J2ZvbnQtc2l6ZToxMC4wcHQ7Zm9udC1mYW1pbHk6IlRh" +
    "aG9tYSIsInNhbnMtc2VyaWYiJz5Gcm9tOjwvc3Bhbj48L2I+PHNwYW4gbGFuZz1FTi1VUyBz" +
    "dHlsZT0nZm9udC1zaXplOjEwLjBwdDtmb250LWZhbWlseToiVGFob21hIiwic2Fucy1zZXJp" +
    "ZiInPiBDYW1lcm9uIEFkYW1zIFttYWlsdG86Y2FtZXJvbkB0aGVtYW5pbmJsdWUuY29tXSA8" +
    "YnI+PGI+U2VudDo8L2I+IFR1ZXNkYXksIDI0IEphbnVhcnkgMjAxMiAxMDo1NyBBTTxicj48" +
    "Yj5Ubzo8L2I+IEJlbiBHcnViYjxicj48Yj5TdWJqZWN0OjwvYj4gRmx1ZW50IGludGVydmll" +
    "dzxvOnA+PC9vOnA+PC9zcGFuPjwvcD48L2Rpdj48cCBjbGFzcz1Nc29Ob3JtYWw+PG86cD4m" +
    "bmJzcDs8L286cD48L3A+PHAgY2xhc3M9TXNvTm9ybWFsPkhpIEJlbiw8bzpwPjwvbzpwPjwv" +
    "cD48ZGl2PjxwIGNsYXNzPU1zb05vcm1hbD48bzpwPiZuYnNwOzwvbzpwPjwvcD48L2Rpdj48" +
    "ZGl2PjxwIGNsYXNzPU1zb05vcm1hbD5JIHdhcyB3b3JraW5nIHRpbGwgM2FtIHRoaXMgbW9y" +
    "bmluZywgc28geW91IGNhdWdodCBtZSBoYWxmIGFzbGVlcCA6KTxvOnA+PC9vOnA+PC9wPjwv" +
    "ZGl2PjxkaXY+PHAgY2xhc3M9TXNvTm9ybWFsPjxvOnA+Jm5ic3A7PC9vOnA+PC9wPjwvZGl2" +
    "PjxkaXY+PHAgY2xhc3M9TXNvTm9ybWFsPkp1c3QgdG8gY2xhcmlmeSwgdGhlIDMgbWFpbiBm" +
    "ZWF0dXJlcyB0aGF0IHdlJ3JlIGZvY3VzaW5nIG9uIGZvciBvdXIgaW5pdGlhbCByZWxlYXNl" +
    "IGFyZTo8bzpwPjwvbzpwPjwvcD48L2Rpdj48ZGl2PjxwIGNsYXNzPU1zb05vcm1hbD48bzpw" +
    "PiZuYnNwOzwvbzpwPjwvcD48L2Rpdj48ZGl2PjxwIGNsYXNzPU1zb05vcm1hbD4tIFN0cmVh" +
    "bWxpbmluZyB5b3VyIGVtYWlsIHdvcmtmbG93OiBlYXN5IGNvbnN1bXB0aW9uIG9mIHlvdXIg" +
    "bWFpbCBpbiBhIHN0cmVhbSAoc2hvd2luZyBjb252ZXJzYXRpb25zICZhbXA7IHJlcGxpZXMp" +
    "LCBxdWljayByZXBseWluZyAmYW1wOyBhcmNoaXZpbmcsICZxdW90O2RlYWwgd2l0aCB0aGlz" +
    "IGxhdGVyJnF1b3Q7IGZvciBwb3N0cG9uaW5nIGRlY2lzaW9ucy48bzpwPjwvbzpwPjwvcD48" +
    "L2Rpdj48ZGl2PjxwIGNsYXNzPU1zb05vcm1hbD48bzpwPiZuYnNwOzwvbzpwPjwvcD48L2Rp" +
    "dj48ZGl2PjxwIGNsYXNzPU1zb05vcm1hbD4tIE11bHRpcGxlIGFjY291bnRzOiBtYW5hZ2lu" +
    "ZyBtdWx0aXBsZSBlbWFpbCBhY2NvdW50cyAocGFydGljdWxhcmx5IG9uIEdtYWlsKSBpcyBw" +
    "YWluZnVsLCBzbyB3ZSd2ZSBtYWRlIGl0IGEgcGFpbi1mcmVlIGV4cGVyaWVuY2UgdG8gcmVh" +
    "ZCAmYW1wOyByZXNwb25kIHRvIGFsbCB5b3VyIGFjY291bnRzIGluIHRoZSBvbmUgaW50ZXJm" +
    "YWNlLjxvOnA+PC9vOnA+PC9wPjwvZGl2PjxkaXY+PHAgY2xhc3M9TXNvTm9ybWFsPjxvOnA+" +
    "Jm5ic3A7PC9vOnA+PC9wPjwvZGl2PjxkaXY+PHAgY2xhc3M9TXNvTm9ybWFsPi0gQmV0dGVy" +
    "IHNlYXJjaDogZmFzdGVyIGFuZCBtb3JlIGZvY3VzZWQuIFdlIGVtcGxveSBpbnN0YW50IHNl" +
    "YXJjaCB0byBzaG93IHlvdSByZXN1bHRzIGFzIHlvdSB0eXBlIGFuZCByZXR1cm4gYmV0dGVy" +
    "IHJlc3VsdHMgaGlnaGxpZ2h0aW5nIHdoYXQgeW91J3JlIGxvb2tpbmcgZm9yLjxvOnA+PC9v" +
    "OnA+PC9wPjwvZGl2PjxkaXY+PHAgY2xhc3M9TXNvTm9ybWFsPjxvOnA+Jm5ic3A7PC9vOnA+" +
    "PC9wPjwvZGl2PjxkaXY+PHAgY2xhc3M9TXNvTm9ybWFsPlRoZSBuYW1lcyBvZiB0aGUgb3Ro" +
    "ZXIgdHdvIHBlb3BsZSBpbiB0aGUgYnVzaW5lc3MgYXJlIERoYW5qaSBQcmFzYW5uYSAmYW1w" +
    "OyBKb2NoZW4gQmVrbWFubi48bzpwPjwvbzpwPjwvcD48L2Rpdj48ZGl2PjxwIGNsYXNzPU1z" +
    "b05vcm1hbD48bzpwPiZuYnNwOzwvbzpwPjwvcD48L2Rpdj48ZGl2PjxwIGNsYXNzPU1zb05v" +
    "cm1hbD5BcyBJIG1lbnRpb25lZCB0byB5b3UsIHdlJ3ZlIG9ubHkgaGFuZGVkIG91dCBhY2Nv" +
    "dW50cyB0byBjbG9zZSB0ZXN0ZXJzIGFuZCB3ZSB0YWxrZWQgdG8gQXNoZXIgYWJvdXQgZ2l2" +
    "aW5nIHlvdSBndXlzIGFuIGV4Y2x1c2l2ZSBhcm91bmQgbGF1bmNoIHRpbWUgLS0gRmVicnVh" +
    "cnkgMTQuIEknbGwgZ2V0IHlvdSBhbiBpbnZpdGF0aW9uIEFTQVAuPG86cD48L286cD48L3A+" +
    "PC9kaXY+PGRpdj48cCBjbGFzcz1Nc29Ob3JtYWw+PG86cD4mbmJzcDs8L286cD48L3A+PC9k" +
    "aXY+PGRpdj48cCBjbGFzcz1Nc29Ob3JtYWw+QW55IHF1ZXN0aW9ucywgbGV0IG1lIGtub3cu" +
    "PG86cD48L286cD48L3A+PC9kaXY+PGRpdj48cCBjbGFzcz1Nc29Ob3JtYWw+LS08bzpwPjwv" +
    "bzpwPjwvcD48L2Rpdj48ZGl2PjxwIGNsYXNzPU1zb05vcm1hbD5DYW1lcm9uIEFkYW1zPG86" +
    "cD48L286cD48L3A+PC9kaXY+PGRpdj48cCBjbGFzcz1Nc29Ob3JtYWw+PG86cD4mbmJzcDs8" +
    "L286cD48L3A+PC9kaXY+PGRpdj48cCBjbGFzcz1Nc29Ob3JtYWw+VHdpdHRlcjogaHR0cDov" +
    "L3R3aXR0ZXIuY29tL3RoZW1hbmluYmx1ZTxvOnA+PC9vOnA+PC9wPjwvZGl2PjxkaXY+PHAg" +
    "Y2xhc3M9TXNvTm9ybWFsPldlYjogaHR0cDovL3d3dy50aGVtYW5pbmJsdWUuY29tPG86cD48" +
    "L286cD48L3A+PC9kaXY+PC9kaXY+PHA+PC9wPjxwPjwvcD48cD48L3A+PHA+PGJyPjxicj48" +
    "YnI+DQo8aHIgc2l6ZT0yIHdpZHRoPSIxMDAlIiBhbGlnbj1jZW50ZXIgdGFiaW5kZXg9LTE+" +
    "DQo8Zm9udCBmYWNlPUFyaWFsIHNpemU9MT5UaGUgaW5mb3JtYXRpb24gY29udGFpbmVkIGlu" +
    "IHRoaXMgZS1tYWlsIG1lc3NhZ2UgYW5kIGFueSBhY2NvbXBhbnlpbmcgZmlsZXMgaXMgb3Ig" +
    "bWF5IGJlIGNvbmZpZGVudGlhbC4gSWYgeW91IGFyZSBub3QgdGhlIGludGVuZGVkIHJlY2lw" +
    "aWVudCwgYW55IHVzZSwgZGlzc2VtaW5hdGlvbiwgcmVsaWFuY2UsIGZvcndhcmRpbmcsIHBy" +
    "aW50aW5nIG9yIGNvcHlpbmcgb2YgdGhpcyBlLW1haWwgb3IgYW55IGF0dGFjaGVkIGZpbGVz" +
    "IGlzIHVuYXV0aG9yaXNlZC4gVGhpcyBlLW1haWwgaXMgc3ViamVjdCB0byBjb3B5cmlnaHQu" +
    "IE5vIHBhcnQgb2YgaXQgc2hvdWxkIGJlIHJlcHJvZHVjZWQsIGFkYXB0ZWQgb3IgY29tbXVu" +
    "aWNhdGVkIHdpdGhvdXQgdGhlIHdyaXR0ZW4gY29uc2VudCBvZiB0aGUgY29weXJpZ2h0IG93" +
    "bmVyLiBJZiB5b3UgaGF2ZSByZWNlaXZlZCB0aGlzIGUtbWFpbCBpbiBlcnJvciBwbGVhc2Ug" +
    "YWR2aXNlIHRoZSBzZW5kZXIgaW1tZWRpYXRlbHkgYnkgcmV0dXJuIGUtbWFpbCBvciB0ZWxl" +
    "cGhvbmUgYW5kIGRlbGV0ZSBhbGwgY29waWVzLiBGYWlyZmF4IE1lZGlhIGRvZXMgbm90IGd1" +
    "YXJhbnRlZSB0aGUgYWNjdXJhY3kgb3IgY29tcGxldGVuZXNzIG9mIGFueSBpbmZvcm1hdGlv" +
    "biBjb250YWluZWQgaW4gdGhpcyBlLW1haWwgb3IgYXR0YWNoZWQgZmlsZXMuIEludGVybmV0" +
    "IGNvbW11bmljYXRpb25zIGFyZSBub3Qgc2VjdXJlLCB0aGVyZWZvcmUgRmFpcmZheCBNZWRp" +
    "YSBkb2VzIG5vdCBhY2NlcHQgbGVnYWwgcmVzcG9uc2liaWxpdHkgZm9yIHRoZSBjb250ZW50" +
    "cyBvZiB0aGlzIG1lc3NhZ2Ugb3IgYXR0YWNoZWQgZmlsZXMuPC9mb250Pg0KPGhyIHNpemU9" +
    "MiB3aWR0aD0iMTAwJSIgYWxpZ249Y2VudGVyIHRhYmluZGV4PS0xPg0KPC9ib2R5PjwvaHRt" +
    "bD4=";

    String out = IOUtils.toString( MimeUtility.decode(new ByteArrayInputStream(sample.getBytes("utf-8")), "base64"));
    System.out.println("###: " + out);
  }
}
