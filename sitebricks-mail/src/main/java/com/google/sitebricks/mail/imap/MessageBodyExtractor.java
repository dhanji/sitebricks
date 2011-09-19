package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.codec.DecoderUtil;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
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
  private static final String BOUNDARY_PREFIX = "boundary=";
  static final Pattern MESSAGE_START_REGEX = Pattern.compile("^\\* \\d+ FETCH \\(BODY\\[\\]",
      Pattern.CASE_INSENSITIVE);
  static final Pattern EOS_REGEX =
      Pattern.compile("^\\d+ ok success\\)?", Pattern.CASE_INSENSITIVE);
  private static final Pattern WHITESPACE_PREFIX_REGEX = Pattern.compile("^\\s+");


  @Override
  public List<Message> extract(List<String> messages) {
    List<Message> emails = Lists.newArrayList();
    ListIterator<String> iterator = messages.listIterator();

    while (iterator.hasNext())
      emails.add(parseMessage(iterator));

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
      } else if (MESSAGE_START_REGEX.matcher(next).find()) {
        iterator.previous();
        break;
      }
    }

    return email;
  }

  private static String mimeType(Multimap<String, String> headers) {
    Collection<String> mimeType = headers.get("Content-Type");
    if (mimeType.isEmpty())
      return "text/plain";    // Default to text plain mimetype.
    return Parsing.stripQuotes(mimeType.iterator().next().toLowerCase());
  }

  private static void parseBodyParts(ListIterator<String> iterator, HasBodyParts entity,
                                     String mimeType, String boundary) {
    if (mimeType.startsWith("text/plain") || mimeType.startsWith("text/html")) {
      String body = readBodyAsString(iterator, boundary);

      // There should only be one of these.
      Collection<String> encodingHeaders = entity.getHeaders().get("Content-Transfer-Encoding");
      String encoding;
      if (encodingHeaders.isEmpty())
        encoding = "7bit"; // default to 7-bit as per the MIME RFC.
      else
        encoding = encodingHeaders.iterator().next();

      entity.setBody(decode(body, encoding, charset(mimeType)));
    } else if (mimeType.startsWith("multipart/") /* mixed|alternative */) {
      String boundaryToken = boundary(mimeType);

      // Skip everything upto the first occurrence of boundary (called the "Preamble")
      //noinspection StatementWithEmptyBody
      while (iterator.hasNext() && !boundaryToken.equalsIgnoreCase(iterator.next()));

      // Now parse the multipart body in sequence, recursing down as needed...
      while (iterator.hasNext()) {
        Message.BodyPart bodyPart = new Message.BodyPart();
        entity.getBodyParts().add(bodyPart);

        // OK now we're in the mime stream. It may have headers.
        parseHeaderSection(iterator, bodyPart.getHeaders());

        // And parse the body itself (seek up to the next occurrence of boundary token).
        // Recurse down this method to slurp up different content types.
        String partMimeType = mimeType(bodyPart.getHeaders());
        String innerBoundary = boundary(partMimeType);

        // If the internal body part is not multipart alternative, then use the parent boundary.
        if (innerBoundary == null) {
          innerBoundary = boundary;
        }

        // Is this going to be a multi-level recursion?
        if (partMimeType.startsWith("multipart/"))
          bodyPart.setBodyParts(new ArrayList<Message.BodyPart>());

        parseBodyParts(iterator, bodyPart, partMimeType, innerBoundary);

        // we're only done if the last line has a terminal suffix of '--'
        String lastLineRead = iterator.previous();
        // Yes this is the end. Otherwise continue!
        if (Parsing.startsWithIgnoreCase(lastLineRead, boundary + "--")) {
          iterator.next();
          break;
        } else if (isEndOfMessage(iterator, iterator.next(), boundary)) {
          break;
        } else
          iterator.next();
      }

    } else {
      entity.setBody(readBodyAsBytes(iterator, boundary));
    }
  }

  private static String charset(String mimeType) {
    int i = mimeType.indexOf("charset=");
    if (i == -1)
      return "UTF-8";

    int end = mimeType.indexOf(";", i);
    if (end == -1)
      end = mimeType.length();
    mimeType = mimeType.substring(i + "charset=".length(), end);

    return Parsing.stripQuotes(mimeType);
  }

  private static String decode(String body, String encoding, String charset) {
    try {
      return IOUtils.toString(
          MimeUtility.decode(new ByteArrayInputStream(body.getBytes()), encoding), charset);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (MessagingException e) {
      throw new RuntimeException(e);
    }
  }

  private static String boundary(String mimeType) {
    int boundaryIndex = mimeType.indexOf(BOUNDARY_PREFIX);
    if (boundaryIndex == -1)
      return null;
    String boundary = mimeType.substring(boundaryIndex + BOUNDARY_PREFIX.length());

    // Strip quotes. Apparently the quotes that the header comes in with are not necessarily part
    // of the boundary token. Sigh.
    boundary = Parsing.stripQuotes(boundary);
    return "--" + boundary;
  }

  private static byte[] readBodyAsBytes(ListIterator<String> iterator, String boundary) {
    return readBodyAsString(iterator, boundary).getBytes();
  }

  private static String readBodyAsString(ListIterator<String> iterator, String boundary) {
    StringBuilder textBody = new StringBuilder();
    // Parse as plain text.
    while (iterator.hasNext()) {
      String line = iterator.next();
      if (boundary != null && line.startsWith(boundary)) {
        // end of section.
        return textBody.toString();
      } else {
        // Check for IMAP command stream delimiter.
        if (isEndOfMessage(iterator, line, boundary))
          return textBody.toString();
      }
      textBody.append(line).append("\r\n");
    }
    return textBody.toString();
  }

  private static boolean isEndOfMessage(ListIterator<String> iterator,
                                        String line, String boundary) {
    // It's possible for the ) to occur on its own line, or on the same line as the boundary marker.
    if (")".equals(line) || (boundary != null && ")".equals(
        line.replace(boundary + "--", "").trim()))) {
      if (iterator.hasNext()) {
        String next = iterator.next();
        if (EOS_REGEX.matcher(next).matches())
          return true;
        if (MESSAGE_START_REGEX.matcher(next).find()) {
          iterator.previous();
          return true;
        } else  // oops, go back.
          iterator.previous();
      }
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
    String[] split = message.split(": ", 2);
    String value = split[1];

    // Check if the next line begins with a LWSP. If it does, then it is a continuation of this
    // line.
    // This is called "Unfolding" as per RFC 822. http://www.faqs.org/rfcs/rfc822.html
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
