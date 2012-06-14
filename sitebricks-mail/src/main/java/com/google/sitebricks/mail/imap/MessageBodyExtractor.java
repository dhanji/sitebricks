package com.google.sitebricks.mail.imap;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts a full Message body from an IMAP fetch. Specifically
 * a "fetch body[]" command which comes back with the raw content of the
 * message including all headers and mime body parts.
 * <p/>
 * A faster, lighter form of fetch exists for message status info which
 * would contain flags, recipients, subject, etc.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MessageBodyExtractor implements Extractor<List<Message>> {
  private static final Logger log = LoggerFactory.getLogger(MessageBodyExtractor.class);
  private static final Logger parseErrorLog = LoggerFactory.getLogger("parseerrordump");

  static final Pattern BOUNDARY_REGEX = Pattern.compile(
      ";[\\s]*boundary[\\s]*=[\\s]*[\"]?([^\"^;]*)[\"]?",
      Pattern.CASE_INSENSITIVE);
  static final Pattern CHARSET_REGEX =
      Pattern.compile(";[\\s]*charset[\\s]*=[\\s]*[\"]?([^\"^;]*)[\"]?",
      Pattern.CASE_INSENSITIVE);
  static final Pattern MESSAGE_START_PREFIX_REGEX = Pattern.compile("^\\* \\d+ FETCH \\(UID \\d+ BODY\\[\\]",
      Pattern.CASE_INSENSITIVE);
  static final Pattern MESSAGE_START_REGEX = Pattern.compile("[*] \\d+ FETCH \\(UID \\d+ BODY\\[\\] " +
      "\\{(\\d+)\\}\\s*",
      Pattern.CASE_INSENSITIVE);

  static final Pattern EOS_REGEX =
      Pattern.compile("^\\d+ ok success\\)?", Pattern.CASE_INSENSITIVE);
  private static final Pattern WHITESPACE_PREFIX_REGEX = Pattern.compile("^\\s+");

  private static final Map<String, String> CONVERTIBLE_CHARSETS = Maps.newHashMap();
  private static final Map<String, String> CONVERTIBLE_ENCODINGS = Maps.newHashMap();
  private static final String SEVEN_BIT = "7bit";
  private static final String EIGHT_BIT = "8bit";
  private static final String UTF_8 = "UTF-8";

  static {
    CONVERTIBLE_CHARSETS.put("cp932", "cp942");
    CONVERTIBLE_CHARSETS.put("5035", "cp939");
    CONVERTIBLE_CHARSETS.put("5033", "cp937");
    CONVERTIBLE_CHARSETS.put("5031", "cp935");
    CONVERTIBLE_CHARSETS.put("5026", "cp930");
    CONVERTIBLE_CHARSETS.put("5029", "cp933");
    CONVERTIBLE_CHARSETS.put("938", "cp948");
    CONVERTIBLE_CHARSETS.put("5050", "cp33722");

    CONVERTIBLE_ENCODINGS.put("7bitmime", "7bit");
    CONVERTIBLE_ENCODINGS.put("7-bitmime", "7bit");
    CONVERTIBLE_ENCODINGS.put("7-bit", "7bit");
    CONVERTIBLE_ENCODINGS.put("8bitmime", "8bit");
    CONVERTIBLE_ENCODINGS.put("8-bitmime", "8bit");
    CONVERTIBLE_ENCODINGS.put("8-bit", "8bit");
    CONVERTIBLE_ENCODINGS.put("base64mime", "base64");
    CONVERTIBLE_ENCODINGS.put("quotedprintable", "quoted-printable");
    CONVERTIBLE_ENCODINGS.put("quotedprintablemime", "quoted-printable");
    CONVERTIBLE_ENCODINGS.put("quoted-printable-mime", "quoted-printable");
    CONVERTIBLE_ENCODINGS.put("quoted-printablemime", "quoted-printable");

    // Add uncompliant and broken transfer encodings, as seen in the wild:
    CONVERTIBLE_ENCODINGS.put("text/plain", "7bit");
  }

  private final boolean forceTruncatorGroping;
  private final long ignoreMessageBodyLengthForTesting;

  // Special constructor for testing only.
  @TestOnly
  MessageBodyExtractor(boolean forceTruncatorGroping, long ignoreMessageBodyLengthForTesting) {
    this.forceTruncatorGroping = forceTruncatorGroping;
    this.ignoreMessageBodyLengthForTesting = ignoreMessageBodyLengthForTesting;
  }

  MessageBodyExtractor() {
    forceTruncatorGroping = false;
    ignoreMessageBodyLengthForTesting = 0;
  }

  @Override
  public List<Message> extract(List<String> messages) {
    List<Message> emails = Lists.newArrayList();

    // Partition the incoming message set into individual message blocks.
    // We do this to prevent errors in one individual message causing the entire
    // batch to be corrupted.
    List<List<String>> partitionedMessagesSet = Lists.newArrayList();
    int start = 0;
    for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
      String message = messages.get(i);
      final Matcher matcher = MESSAGE_START_REGEX.matcher(message);
      if (matcher.matches() && i > start) {
        try {
          int msgLen = Integer.parseInt(matcher.group(1));
        } catch(NumberFormatException e) {
          log.error("Got message with invalid length specification: {} dumping 5 lines...",
              matcher.group(1) );
          for (int j = i; j < messages.size() && j - i < 5; j++) {
            log.error(messages.get(j));
          }
        }
        // Partition.
        partitionedMessagesSet.add(messages.subList(start, i));
        start = i;
      }
    }

    // Pick up the trailing bit if any.
    if (start < messages.size() - 1)
      partitionedMessagesSet.add(messages.subList(start, messages.size()));

    for (List<String> partitionedMessages : partitionedMessagesSet) {
      ListIterator<String> iterator = partitionedMessages.listIterator();
      try {
        // Count errors as you go, don't throw exceptions s.t. we can carry on parsing and
        // yet give error report at high level.
        AtomicInteger errorCount = new AtomicInteger();
        errorCount.set(0);
        Message message = parseMessage(iterator, errorCount);
        // Messages may be null if there are gaps in the returned body. These should be safe.
        if (null != message)
          emails.add(message);

        if (errorCount.get() > 0) {
          // Instead add a sentinel for this message.
          dumpError(emails, partitionedMessages, errorCount.get());
        }
        // Jochen: remove this as soon as satisfied this is safe. (should be, there should never be another FETCH
        // or interesting stuff trailing the mail).
        while (iterator.hasNext()) {
          String next = iterator.next();
          if (!EOS_REGEX.matcher(next).matches()) {
            log.warn("Suspect line trailing id: {} line: {}", message.getImapUid(), next);
          }
        }
      } catch (RuntimeException e) {
        log.error("Unexpected error while parsing message", e);
        emails.add(Message.ERROR);
        e.printStackTrace();
        dumpError(emails, partitionedMessages, 1);
      }
    }

    return emails;
  }

  private void dumpError(List<Message> emails, List<String> partitionedMessages, int errorCount) {
    log.error("{} Message parsing error(s) encountered in emails. See parse_error_log dump for details.", errorCount);
    parseErrorLog.error("===========");
    parseErrorLog.error("{} Message parsing error(s) encountered in emails.", errorCount);
    for (String piece : partitionedMessages) {
      parseErrorLog.info(piece);
    }
  }

  private ListIterator<String> selectLengthBasedSection(ListIterator<String> iterator, long msgSize) throws ParseException {
    // Trim the message according to the length, and get on with parsing.
    List<String> lengthTruncated = Lists.newLinkedList();
    int lines = 0;
    try {
      long len = 0;
      String s = "";
      while (iterator.hasNext()) {
        String last = s;
        s = iterator.next();
        lines++;
        len += s.length();
        if (len <= msgSize) {
          lengthTruncated.add(s);
          len += 2;
        } else {
          if (!s.endsWith(")")) {
            if (EOS_REGEX.matcher(s).matches() && last.endsWith(")")) // Seen in wild, when length is under-estimated.
              break;

            log.info("Invalid email length passed len:{} size:{} s:\"{}\"", new Object[]{len, msgSize, s});
            iterator.previous();
            iterator.previous();
            log.info("Invalid length context: {}", iterator.next());
            log.info("Invalid length context: {}", iterator.next());
            throw new ParseException("Invalid email length passed, actual len: " + len + " msg size: " + msgSize +
                " s: \"" + s + "\". Reverting to default parsing", lines);
          }
          lengthTruncated.add(s);
          break;
        }
      }
  
      return lengthTruncated.listIterator();
    } catch(ParseException e) {
      // reset iterator if we failed.
      while (lines-- > 0)
        iterator.previous();
      throw e;
    }
  }

  private Message parseMessage(ListIterator<String> iterator, AtomicInteger errorCount) {
    Message email = new Message();
    // Read the leading message (command response).
    String firstLine = iterator.next();
    // It is possible that the requested message stream is completely empty.
    if (EOS_REGEX.matcher(firstLine).matches())
      return null;

    firstLine = firstLine.replaceFirst("[*]? \\d+[ ]* ", "");
    Queue<String> tokens = Parsing.tokenize(firstLine);
    Parsing.eat(tokens, "FETCH", "(", "UID");
    email.setImapUid(Parsing.match(tokens, int.class));
    Parsing.eat(tokens, "BODY[]");
    String sizeString = Parsing.match(tokens, String.class);
    long size = 0;
    boolean gropeForTruncator = true;
    boolean moreErrorInfo = false;

    // Parse out size in bytes from "{NNN}"
    if (sizeString != null && sizeString.length() > 0) {
      try {
        size = Long.parseLong(sizeString.substring(1, sizeString.length() - 1));
      } catch(NumberFormatException e) {
        log.warn("Internal error: regex match should never have passed invalid number string: {}", sizeString);
        moreErrorInfo = true;
      }
    }

    gropeForTruncator = forceTruncatorGroping || size == 0 || size == ignoreMessageBodyLengthForTesting;

    if (!gropeForTruncator) {
      try {
        iterator = selectLengthBasedSection(iterator, size);
      } catch (ParseException e) {
        log.warn(e.getMessage());
        gropeForTruncator = true;
        moreErrorInfo = true;
      }
    }

    // OK now parse the header stream.
    // Don't pass gropeForTruncator, in case there's an error and we get an abridged email,
    //  we don't expect rogue terminators in the header section, so play it safe.
    parseHeaderSection(iterator, email.getHeaders(), null, errorCount);

    // OK now parse the body/mime stream...
    // First determine the mimetype.
    String mimeType = mimeType(email.getHeaders());

    // Normalize mimetype case.
    mimeType = mimeType.toLowerCase();
    parseBodyParts(iterator, email, mimeType, boundary(mimeType), errorCount, gropeForTruncator);

    if (moreErrorInfo) {
      log.warn("previous error pertained to email with uid: {} headers: {}", email.getImapUid(), email.getHeaders());
    }
    return email;
  }

  static String mimeType(Multimap<String, String> headers) {
    Collection<String> mimeType = Parsing.getKeyVariations(headers, "Content-Type", "Content-type", "content-type");
    if (mimeType.isEmpty())
      return "text/plain";    // Default to text plain mimetype.
    return Parsing.stripQuotes(mimeType.iterator().next().toLowerCase().trim()).trim();
  }

  private static String transferEncoding(HasBodyParts entity) {
    if (null == entity.getHeaders())
      return SEVEN_BIT;
    Collection<String> values = Parsing.getKeyVariations(entity.getHeaders(), "Content-Transfer-Encoding",
        "Content-transfer-encoding", "Content-Transfer-encoding", "content-transfer-encoding");
    if (values.isEmpty())
      return SEVEN_BIT;

    String transferEncoding = values.iterator().next().trim();

    // Seek upto ; in case this is split.
    int end = transferEncoding.indexOf(";");
    if (end > -1)
      transferEncoding = transferEncoding.substring(0, end);

    int start = 0;
    if (transferEncoding.startsWith("\""))
      start = 1;
    if (transferEncoding.endsWith("\""))
      end = transferEncoding.length() - 1;
    else
      end = transferEncoding.length();

    // Strip quotes.
    if (start > 0 || end < transferEncoding.length())
      transferEncoding = transferEncoding.substring(start, end);

    if (transferEncoding.isEmpty())
      return SEVEN_BIT;

    transferEncoding = transferEncoding.toLowerCase();
    String alternate = CONVERTIBLE_ENCODINGS.get(transferEncoding);
    return (alternate != null) ? alternate : transferEncoding;
  }

  private static boolean isAttachment(Multimap<String, String> headers) {
    Collection<String> values = Parsing.getKeyVariations(headers, "Content-Disposition",
        "Content-disposition", "content-disposition");
    if (values.isEmpty())
      return false;

    String value = values.iterator().next().trim().toLowerCase();
    return value.contains("attachment") || value.contains("filename");
  }

  /**
   * @return whether a boundary end marker was encountered.
   */
  private static boolean parseBodyParts(ListIterator<String> iterator, HasBodyParts entity,
                                        String mimeType, String boundary, AtomicInteger errorCount,
                                        boolean gropeForTruncator) {

    if (mimeType.startsWith("text/") && !isAttachment(entity.getHeaders())) {
      String body = readBodyAsString(iterator, boundary, gropeForTruncator);

      entity.setBody(decode(body, transferEncoding(entity), charset(mimeType)));
    } else if (mimeType.startsWith("multipart/") /* mixed|alternative|digest */) {
      String boundaryToken = boundary(mimeType);

      if (boundaryToken == null) {
        throw new RuntimeException("Encountered multipart with no boundary token defined for " +
            mimeType);
      }

      // For the record: http://tools.ietf.org/html/rfc2045#section-5.1
      // specifies that boundaries are case sensitive. We, however do case-insensitive
      // comparison....

      // Skip everything upto the first occurrence of boundary (called the "Preamble")
      //noinspection StatementWithEmptyBody
      while (iterator.hasNext() && !boundaryToken.equalsIgnoreCase(iterator.next())) ;

      // Now parse the multipart body in sequence, recursing down as needed...
      while (iterator.hasNext()) {
        Message.BodyPart bodyPart = new Message.BodyPart();
        entity.createBodyParts();
        entity.getBodyParts().add(bodyPart);

        // OK now we're in the mime stream. It may have headers.
        parseHeaderSection(iterator, bodyPart.getHeaders(), null, errorCount);

        // And parse the body itself (seek up to the next occurrence of boundary token).
        // Recurse down this method to slurp up different content types.
        String partMimeType = mimeType(bodyPart.getHeaders());
        String innerBoundary = boundary(partMimeType);

        // If the internal body part is not multipart alternative, then use the parent boundary.
        if (innerBoundary == null)
          innerBoundary = boundaryToken;

        // Is this going to be a multi-level recursion?
        if (partMimeType.startsWith("multipart/"))
          bodyPart.createBodyParts();

        // If the inner body was parsed up until we reached boundary end marker, ending with "--"
        // then skip everything until we see a start boundary marker.
        if (parseBodyParts(iterator, bodyPart, partMimeType, innerBoundary, errorCount, gropeForTruncator)) {
          //noinspection StatementWithEmptyBody
          while (iterator.hasNext() && !Parsing.startsWithIgnoreCase(iterator.next(),
              boundaryToken)) ;
        }

        // we're only done if the last line has a terminal suffix of '--'
        String lastLineRead = iterator.previous();
        // Yes this is the end. Otherwise continue!
        if (Parsing.startsWithIgnoreCase(lastLineRead, boundaryToken + "--")) {
          iterator.next();
          return true;
        } else if (hasImapTerminator(iterator, iterator.next(), gropeForTruncator)) {
          break;
        }
      }
    } else if (mimeType.startsWith("message/rfc822")) {

      // These are encapsulated messages. I.e. a message inside a part. Go figure.
      // We store them as a child body part, with the containing part having no body of its own,
      // merely the headers.
      Message.BodyPart bodyPart = new Message.BodyPart();
      entity.createBodyParts();
      entity.getBodyParts().add(bodyPart);

      String bodyEncoding = transferEncoding(entity);

      ListIterator<String> rfc822iterator = iterator;
      // First decode the body according to the content-transfer-encoding, then parse
      // the embedded message.

      boolean alreadyHitEndMarker = false;
      final boolean quotedPrintable = "quoted-printable".equals(bodyEncoding);

      // For quoted-printable do the efficient thing, just decode each line separately.
      if (quotedPrintable) {
        List<String> rfc822msg = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
          final String s = iterator.next();
          if (hasImapTerminator(iterator, s, gropeForTruncator) ||
              boundary != null && Parsing.startsWithIgnoreCase(s, boundary)) {
            alreadyHitEndMarker = Parsing.startsWithIgnoreCase(s, boundary + "--");
            if (sb.length() > 0) // save dangly bit, though technically illegal here.
              rfc822msg.add(sb.toString());
            break;
          }
          if (s.endsWith("=")) {
            sb.append(s);
            sb.setLength(sb.length() - 1); // trim the trailing '='.
          } else if (sb.length() > 0) {  // previous line(s) had a soft line break.
            sb.append(s);
            rfc822msg.add(sb.toString());
            sb.setLength(0);
          } else {
            rfc822msg.add(s);
          }
          // Don't need to decode s before checking for boundary as the boundary is not part of the
          // encoded body.
        }
        rfc822iterator = decode(rfc822msg, "quoted-printable", charset(mimeType)).listIterator();
      } else if(SEVEN_BIT.equals(bodyEncoding) || EIGHT_BIT.equals(bodyEncoding) || "binary".equals(bodyEncoding)) {
        // No decoding needed.

      } else {
        // Unsupported encoding, print out error context and skip to end of part/message.
        throw new RuntimeException("Unsupported encoding for embedded rfc822 message " +
            bodyEncoding);
      }

      // Parse the body of this message as though it were a new message itself.
      parseHeaderSection(rfc822iterator, bodyPart.getHeaders(), null, errorCount);
      String bodyBoundary = boundary(mimeType);
      boolean gotEndMarker = parseBodyParts(rfc822iterator, bodyPart,
          mimeType(bodyPart.getHeaders()),
          bodyBoundary != null ? bodyBoundary : boundary, errorCount, gropeForTruncator);
      return quotedPrintable ? alreadyHitEndMarker : gotEndMarker;
    } else {
      entity.setBodyBytes(readBodyAsBytes(transferEncoding(entity), iterator, boundary,
          charset(mimeType), errorCount, gropeForTruncator));
    }
    return false;
  }

  static String charset(String mimeType) {
    if (null == mimeType)
      return UTF_8;
    Matcher matcher = CHARSET_REGEX.matcher(mimeType);
    if (!matcher.find())
      return UTF_8;

    String charset = matcher.group(1);
    if (null == charset || charset.isEmpty())
      return UTF_8;

    charset = charset.trim();

    // The Java platform only supports a limited set of encodings, use ones that it supports
    // if we encounter unknown ones.
    String alternate = CONVERTIBLE_CHARSETS.get(charset.toLowerCase());
    return (alternate != null) ? alternate : charset;
  }


  private static List<String> decode(List<String> body, String encoding, String charset) {
    List<String> l = Lists.newArrayList();
    for(String s : body)
      l.add(decode(s, encoding, charset));
    return l;
  }

  private static String decode(String body, String encoding, String charset) {
    try {

      // Second time around. Apparently some are slipping through.
      charset = Parsing.stripQuotes(charset);

      return CharStreams.toString(
          new InputStreamReader(MimeUtility.decode(new ByteArrayInputStream(body.getBytes(charset)), encoding), charset));
    } catch (UnsupportedEncodingException e) {
      // In this case, just return it as is and look it up later.
      log.warn("Encountered unknown encoding '{}'. Treating it as a raw string.", charset, e);
      return body;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }

  // http://tools.ietf.org/html/rfc2046#section-5.1.1
  static String boundary(String mimeType) {
    Matcher matcher = BOUNDARY_REGEX.matcher(mimeType);
    if (!matcher.find())
      return null;

    String boundary = matcher.group(1);
    if (boundary.isEmpty())
      return null;
    return "--" + boundary.trim();
  }

  private static byte[] readBodyAsBytes(String transferEncoding,
                                        ListIterator<String> iterator,
                                        String boundary,
                                        String charset,
                                        AtomicInteger errorCount,
                                        boolean gropeForTruncator) {
    byte[] bytes = new byte[0];
    try {
      bytes = readBodyAsString(iterator, boundary, gropeForTruncator).getBytes(charset);
    } catch (UnsupportedEncodingException e) {
      log.warn("Could not decode body as string due to encoding, using default encoding.", e);
      bytes = readBodyAsString(iterator, boundary, gropeForTruncator).getBytes();
    }

    // Decode if this is encoded as binary-to-text.
    if (null != transferEncoding)
      try {
        bytes = ByteStreams.toByteArray(MimeUtility.decode(new ByteArrayInputStream(bytes),
            transferEncoding));
      } catch (MessagingException e) {
        log.error("Unable to decode message body, proceeding with raw bytes.", e);
        errorCount.incrementAndGet();
      } catch (IOException e) {
        log.error("Unable to decode message body, proceeding with raw bytes.", e);
        errorCount.incrementAndGet();
      }
    return bytes;
  }

  private static String readBodyAsString(ListIterator<String> iterator, String terminator,
                                         boolean gropeForTruncator) {
    StringBuilder textBody = new StringBuilder();
    // Parse as plain text.
    while (iterator.hasNext()) {
      String line = iterator.next();
      if (terminator != null && Parsing.startsWithIgnoreCase(line, terminator)) {
        // end of section.
        return textBody.toString();
      } else {
        // Check for IMAP command stream delimiter.
        if (hasImapTerminator(iterator, line, gropeForTruncator)) {
          Preconditions.checkArgument(terminator == null || !line.contains(terminator + "--"));
          // If this is actually a boundary with the closing ) token, ignore it. Otherwise add it
          // to the body, it's possible for misbehaving clients to generate this kind of body.
          if (!")".equals(line.trim())) {
            textBody.append(line.substring(0, line.lastIndexOf(")")));
          }
          return textBody.toString();
        }
      }
      textBody.append(line).append("\r\n");
    }
    return textBody.toString();
  }

  private static boolean hasImapTerminator(ListIterator<String> iterator,
                                           String line, boolean gropeForTruncator) {
    if (!gropeForTruncator)
      return line.trim().endsWith(")") && !iterator.hasNext();

    // It's possible for the ) to occur on its own line, or on the same line as the boundary marker.
    // It's also possible for misbehaving clients (Google Groups) to generate a ) on the same line
    // as body text. FUCK.
    // It's also possible to have an email containing a closing bracket and "10 OK success" on
    // the next line. We're going to ignore that possible case here.
    if (line.trim().endsWith(")")) {
      if (iterator.hasNext()) {
        String next = iterator.next();
        if (EOS_REGEX.matcher(next).matches())
          return true;
        if (MESSAGE_START_PREFIX_REGEX.matcher(next).find()) {
          iterator.previous();
          return true;
        } else  // oops, go back.
          iterator.previous();
      } else
        return true;    // If there are no more messages we have reached the end!
    }
    return false;
  }

  private static void parseHeaderSection(ListIterator<String> iterator,
                                         Multimap<String, String> headers,
                                         String headerEncoding, AtomicInteger errorCount) {
    while (iterator.hasNext()) {
      String message = iterator.next();
      // Watch for the end of sequence marker. If we see it, the mime-stream is ended.
      try {
        if (Command.isEndOfSequence(message))
          continue;
      } catch (ExtractionException ee) {
        log.error("Warning: error parsing email message body! {}", iterator, ee);
        errorCount.incrementAndGet();
        continue;
      }
      // A blank line indicates end of the header section.
      if (message.isEmpty())
        break;

      parseHeaderPair(message, iterator, headers, headerEncoding, errorCount);
    }
  }

  private static void parseHeaderPair(String message,
                                      ListIterator<String> iterator,
                                      Multimap<String, String> headers,
                                      String headerEncoding,
                                      AtomicInteger errorCount) {
    // Totally empty header line (i.e. stray whitespace).
    if (message.isEmpty())
      return;

    // Some header blocks are malformed, and contain lines that have no ':'
    if (!message.contains(":")) {
      log.warn("Malformed message header encountered at {}: {}. Skipping...",
          iterator.previousIndex(),
          message);
      errorCount.incrementAndGet();
      return;
    }
    // It is possible for the header to have no value.
    String[] split = message.split(": ", 2);
    String value = split.length > 1 ? split[1] : "";

    // Check if the next line begins with a LWSP. If it does, then it is a continuation of this
    // line. This is called "Unfolding" as per RFC 822. http://www.faqs.org/rfcs/rfc822.html
    StringBuilder folded = new StringBuilder(value);

    // First read up to the next header.
    while (iterator.hasNext()) {
      String next = iterator.next();
      if (WHITESPACE_PREFIX_REGEX.matcher(next).find()) {
        folded.append(next);
      } else {
        iterator.previous();
        break;
      }
    }

    // Now unfold using javamail's algorithm.
    value = MimeUtility.unfold(folded.toString());

    // Decode content-transfer-encoding if necessary
    if (headerEncoding != null)
      value = decode(value, headerEncoding, "utf-8");

    // Header values can be specially encoded.
    value = DecoderUtil.decodeEncodedWords(value, DecodeMonitor.SILENT);
    headers.put(split[0], value);
  }
}