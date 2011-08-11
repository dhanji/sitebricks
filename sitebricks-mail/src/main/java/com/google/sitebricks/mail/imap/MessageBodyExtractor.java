package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.codec.DecodeMonitor;
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
  static final Pattern EOS_REGEX = Pattern.compile("^\\d+ ok success", Pattern.CASE_INSENSITIVE);

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
    if (iterator.hasNext() && !EOS_REGEX.matcher(iterator.next()).find())
      iterator.previous();

    return email;
  }

  private static String mimeType(Map<String, String> headers) {
    String mimeType = headers.get("Content-Type");
    if (mimeType == null)
      mimeType = "text/plain";    // Default to text plain mimetype.
    return mimeType;
  }

  private static void parseBodyParts(ListIterator<String> iterator, HasBodyParts entity,
                                     String mimeType, String boundary) {
    if (mimeType.startsWith("text/plain") || mimeType.startsWith("text/html")) {
      String body = readBodyAsString(iterator, boundary);
      String encoding = entity.getHeaders().get("Content-Transfer-Encoding");
      if (null == encoding)
        encoding = "7bit"; // default to 7-bit as per the MIME RFC.
      entity.setBody(decode(body, encoding, charset(mimeType)));
    } else if (mimeType.startsWith("multipart/") /* mixed|alternative */) {
      String boundaryToken = boundary(mimeType);

      // Skip everything upto the first occurrence of boundary (called the "Preamble")
      //noinspection StatementWithEmptyBody
      while (iterator.hasNext() && !boundaryToken.equals(iterator.next()));

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
        if (lastLineRead.startsWith(boundary + "--")) {
          // Yes this is the end. Otherwise continue!
          iterator.next();
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

    return mimeType.substring(i + "charset=".length(), end);
  }

  private static String decode(String body, String encoding, String charset) {
    try {
      return IOUtils.toString(MimeUtility.decode(new ByteArrayInputStream(body.getBytes()), encoding), charset);
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
    return "--" + mimeType.substring(boundaryIndex + BOUNDARY_PREFIX.length());
  }

  private static byte[] readBodyAsBytes(ListIterator<String> iterator, String boundary) {
    StringBuilder builder = new StringBuilder();
    while (iterator.hasNext()) {
      String line = iterator.next();
      if (boundary != null && line.startsWith(boundary)) {
        // end of section.
        break;
      } else  {
        if (isEndOfMessage(iterator, line))
          break;
      }
      builder.append(line).append("\r\n");
    }
    return builder.toString().getBytes();
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
        if (isEndOfMessage(iterator, line))
          return textBody.toString();
      }
      textBody.append(line).append("\r\n");
    }
    return textBody.toString();
  }

  private static boolean isEndOfMessage(ListIterator<String> iterator,
                                        String line) {
    if (")".equals(line)) {
      if (iterator.hasNext()) {
        String next = iterator.next();
        if (EOS_REGEX.matcher(next).find())
          return true;
        if (MESSAGE_START_REGEX.matcher(next).find()) {
          iterator.previous();
          return true;
        }
        else  // oops, go back.
          iterator.previous();
      }
    }
    return false;
  }

  private static void parseHeaderSection(ListIterator<String> iterator,
                                         Map<String, String> headers) {
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
                                      Map<String, String> headers) {
    String[] split = message.split(": ", 2);
    String value = split[1];

    // Check if the next line begins with a LWSP. If it does, then it is a continuation of this
    // line.
    // This is called "Unfolding" as per RFC 822. http://www.faqs.org/rfcs/rfc822.html
    while (iterator.hasNext()) {
      String next = iterator.next();
      if (next.startsWith(" "))
        value += ' ' + next.trim();
      else {
        iterator.previous();
        break;
      }
    }

    // Header values can be specially encoded.
    value = DecoderUtil.decodeEncodedWords(value, DecodeMonitor.SILENT);
    headers.put(split[0], value);
  }
}
