package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
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
  static final Pattern BOUNDARY_REGEX = Pattern.compile(
      ";[\\s]*boundary[\\s]*=[\\s]*[\"]?([^\"^;]*)[\"]?",
      Pattern.CASE_INSENSITIVE);
  static final Pattern CHARSET_REGEX =
      Pattern.compile(";[\\s]*charset[\\s]*=[\\s]*[\"]?([^\"^;]*)[\"]?",
      Pattern.CASE_INSENSITIVE);
  static final Pattern MESSAGE_START_PREFIX_REGEX = Pattern.compile("^\\* \\d+ FETCH \\(BODY\\[\\]",
      Pattern.CASE_INSENSITIVE);
  static final Pattern MESSAGE_START_REGEX = Pattern.compile("[*] \\d+ FETCH \\(BODY\\[\\] \\{\\d+\\}\\s*",
      Pattern.CASE_INSENSITIVE);

  static final Pattern EOS_REGEX =
      Pattern.compile("^\\d+ ok success\\)?", Pattern.CASE_INSENSITIVE);
  private static final Pattern WHITESPACE_PREFIX_REGEX = Pattern.compile("^\\s+");

  private static final Map<String, String> CONVERTIBLE_CHARSETS = Maps.newHashMap();
  private static final Map<String, String> CONVERTIBLE_ENCODINGS = Maps.newHashMap();
  private static final String SEVEN_BIT = "7bit";
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
      if (MESSAGE_START_REGEX.matcher(message).matches() && i > start) {
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
      while (iterator.hasNext()) {
        try {
        Message message = parseMessage(iterator);

        // Messages may be null if there are gaps in the returned body. These should be safe.
        if (null != message)
          emails.add(message);
        } catch (RuntimeException e) {
          log.error("Unexpected error while parsing message", e);
          // Instead add a sentinel for this message.
          emails.add(Message.ERROR);
        }
      }
    }

    return emails;
  }

  private Message parseMessage(ListIterator<String> iterator) {
    Message email = new Message();
    // Read the leading message (command response).
    String firstLine = iterator.next();

    // It is possible that the requested message stream is completely empty.
    if (EOS_REGEX.matcher(firstLine).matches())
      return null;

    firstLine = firstLine.replaceFirst("[*]? \\d+[ ]* ", "");
    Queue<String> tokens = Parsing.tokenize(firstLine);
    Parsing.eat(tokens, "FETCH", "(", "BODY[]");
    String sizeString = Parsing.match(tokens, String.class);
    int size = 0;

    // Parse out size in bytes from "{NNN}"
    if (sizeString != null && sizeString.length() > 2) {
      size = Integer.parseInt(sizeString.substring(1, sizeString.length() - 1));
    }

    // OK now parse the header stream.
    parseHeaderSection(iterator, email.getHeaders());

    // OK now parse the body/mime stream...
    // First determine the mimetype.
    String mimeType = mimeType(email.getHeaders());

    // Normalize mimetype case.
    mimeType = mimeType.toLowerCase();
    parseBodyParts(iterator, email, mimeType, boundary(mimeType));

    // Try to chew up the end of sequence marker if it exists.
    while (iterator.hasNext()) {
      // Chew up all the end of sequence markers, whitespace and garbage at the end of a message,
      // Until we see the start of a new message or the end of the entire sequence.
      String next = iterator.next();
      if (EOS_REGEX.matcher(next).matches()) {
        iterator.previous();
        break;
      } else if (MESSAGE_START_PREFIX_REGEX.matcher(next).find()) {
        iterator.previous();
        break;
      }
    }

    return email;
  }

  static String mimeType(Multimap<String, String> headers) {
    Collection<String> mimeType = headers.get("Content-Type");
    if (mimeType.isEmpty())
      return "text/plain";    // Default to text plain mimetype.
    return Parsing.stripQuotes(mimeType.iterator().next().toLowerCase().trim()).trim();
  }

  private static String transferEncoding(HasBodyParts entity) {
    if (null == entity.getHeaders())
      return SEVEN_BIT;
    Collection<String> values = entity.getHeaders().get("Content-Transfer-Encoding");
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

  private static boolean parseBodyParts(ListIterator<String> iterator, HasBodyParts entity,
                                        String mimeType, String boundary) {
    if (mimeType.startsWith("text/plain") || mimeType.startsWith("text/html")) {
      String body = readBodyAsString(iterator, boundary);

      entity.setBody(decode(body, transferEncoding(entity), charset(mimeType)));
    } else if (mimeType.startsWith("multipart/") /* mixed|alternative|digest */) {
      String boundaryToken = boundary(mimeType);

      // Skip everything upto the first occurrence of boundary (called the "Preamble")
      //noinspection StatementWithEmptyBody
      while (iterator.hasNext() && !boundaryToken.equalsIgnoreCase(iterator.next()));

      // Now parse the multipart body in sequence, recursing down as needed...
      while (iterator.hasNext()) {
        Message.BodyPart bodyPart = new Message.BodyPart();
        entity.createBodyParts();
        entity.getBodyParts().add(bodyPart);

        // OK now we're in the mime stream. It may have headers.
        parseHeaderSection(iterator, bodyPart.getHeaders());

        // And parse the body itself (seek up to the next occurrence of boundary token).
        // Recurse down this method to slurp up different content types.
        String partMimeType = mimeType(bodyPart.getHeaders());
        String innerBoundary = boundary(partMimeType);

        // If the internal body part is not multipart alternative, then use the parent boundary.
        if (innerBoundary == null)
          innerBoundary = boundary;

        // Is this going to be a multi-level recursion?
        if (partMimeType.startsWith("multipart/"))
          bodyPart.createBodyParts();

        // If the inner body was parsed up until we reached boundary end marker, ending with "--"
        // then skip everything until we see a start boundary marker.
        if (parseBodyParts(iterator, bodyPart, partMimeType, innerBoundary)) {
          //noinspection StatementWithEmptyBody
          while (iterator.hasNext() && !Parsing.startsWithIgnoreCase(iterator.next(), boundaryToken));
        }

        // we're only done if the last line has a terminal suffix of '--'
        String lastLineRead = iterator.previous();
        // Yes this is the end. Otherwise continue!
        if (Parsing.startsWithIgnoreCase(lastLineRead, boundary + "--")) {
          iterator.next();
          return true;
        } else if (isEndOfMessage(iterator, iterator.next(), boundary)) {
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

      // Parse the body of this message as though it were a new message itself.
      parseHeaderSection(iterator, bodyPart.getHeaders());
      return parseBodyParts(iterator, bodyPart, mimeType(bodyPart.getHeaders()), boundary);

    } else {
      entity.setBody(readBodyAsBytes(transferEncoding(entity), iterator, boundary));
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

  private static String decode(String body, String encoding, String charset) {
    try {

      // Second time around. Apparently some are slipping through.
      charset = Parsing.stripQuotes(charset);

      return IOUtils.toString(
          MimeUtility.decode(new ByteArrayInputStream(body.getBytes()), encoding), charset);
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

  static String boundary(String mimeType) {
    Matcher matcher = BOUNDARY_REGEX.matcher(mimeType);
    if (!matcher.find())
      return null;

    String boundary = matcher.group(1);
    if (boundary.isEmpty())
      return null;
    return "--" + boundary.trim();
  }

  private static byte[] readBodyAsBytes(String transferEncoding, ListIterator<String> iterator, String boundary) {
    byte[] bytes = readBodyAsString(iterator, boundary).getBytes();

    // Decode if this is encoded as binary-to-text.
    if (null != transferEncoding)
      try {
        bytes = IOUtils.toByteArray(MimeUtility.decode(new ByteArrayInputStream(bytes),
            transferEncoding));
      } catch (MessagingException e) {
        log.error("Unable to decode message body, proceeding with raw bytes.", e);
      } catch (IOException e) {
        log.error("Unable to decode message body, proceeding with raw bytes.", e);
      }
    return bytes;
  }

  private static String readBodyAsString(ListIterator<String> iterator, String boundary) {
    StringBuilder textBody = new StringBuilder();
    // Parse as plain text.
    while (iterator.hasNext()) {
      String line = iterator.next();
      if (boundary != null && Parsing.startsWithIgnoreCase(line, boundary)) {
        // end of section.
        return textBody.toString();
      } else {
        // Check for IMAP command stream delimiter.
        if (isEndOfMessage(iterator, line, boundary)) {

          // If this is actually a boundary with the closing ) token, ignore it. Otherwise add it
          // to the body, it's possible for misbehaving clients to generate this kind of body.
          if (!")".equals(line.replace(boundary + "--", "").trim())) {
            textBody.append(line.substring(0, line.lastIndexOf(")")));
          }
          return textBody.toString();
        }
      }
      textBody.append(line).append("\r\n");
    }
    return textBody.toString();
  }

  private static boolean isEndOfMessage(ListIterator<String> iterator,
                                        String line, String boundary) {
    // It's possible for the ) to occur on its own line, or on the same line as the boundary marker.
    // It's also possible for misbehaving clients (Google Groups) to generate a ) on the same line
    // as body text. FUCK.
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
                                         Multimap<String, String> headers) {
    while (iterator.hasNext()) {
      String message = iterator.next();
      // Watch for the end of sequence marker. If we see it, the mime-stream is ended.
      if (Command.isEndOfSequence(message.replaceFirst("\\d+[ ]* ", "").toLowerCase()))
        continue;

      // A blank line indicates end of the header section.
      if (message.isEmpty())
        break;
      parseHeaderPair(message, iterator, headers);
    }
  }

  private static void parseHeaderPair(String message, ListIterator<String> iterator,
                                      Multimap<String, String> headers) {
    // Totally empty header line (i.e. stray whitespace).
    if (message.isEmpty())
      return;

    // Some header blocks are malformed, and contain lines that have no ':'
    if (!message.contains(":")) {
        log.warn("Malformed message header encountered at {}: {}. Skipping...",
            iterator.previousIndex(),
            message);
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
      if (WHITESPACE_PREFIX_REGEX.matcher(next).find())
        folded.append(next);
      else {
        iterator.previous();
        break;
      }
    }

    // Now unfold using javamail's algorithm.
    value = MimeUtility.unfold(folded.toString());

    // Header values can be specially encoded.
    value = DecoderUtil.decodeEncodedWords(value);
    headers.put(split[0], value);
  }
}
