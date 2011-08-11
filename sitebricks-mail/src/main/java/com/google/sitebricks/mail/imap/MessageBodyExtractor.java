package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Extracts a full Message body from an IMAP fetch. Specifically
 * a "fetch body[]" command which comes back with the raw content of the
 * message including all headers and mime body parts.
 * <p>
 * A faster, lighter form of fetch exists for message status info which
 * would contain flags, recipients, subject, etc.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MessageBodyExtractor implements Extractor<List<Message>> {

  static final DateTimeFormatter RECEIVED_DATE = DateTimeFormat.forPattern(
      "EEE, dd MMM yyyy HH:mm:ss Z");
  static final DateTimeFormatter INTERNAL_DATE = DateTimeFormat.forPattern(
      "dd-MMM-yyyy HH:mm:ss Z");

  @Override
  public List<Message> extract(List<String> messages) {
    List<Message> statuses = Lists.newArrayList();
    StringBuilder builder = new StringBuilder();

    // Skip fetch line..
    System.out.println("rmoving: " + messages.remove(0));
    for (String message : messages) {

      // Discard the fetch token.
      if (Command.isEndOfSequence(message.replaceFirst("\\d+[ ]* ", "").toLowerCase()))
        continue;

      builder.append(message).append("\r\n");
    }
    try {
      org.apache.james.mime4j.dom.Message message = new DefaultMessageBuilder().parseMessage(
          new ByteArrayInputStream(builder.toString().getBytes()));
      System.out.println("--------------------->>>><<<<-----------------------");
      System.out.println(message.getMessageId());
      System.out.println(message.getFrom());
      System.out.println(message.getTo());
      System.out.println(message.getMimeType());
      System.out.println(message.getFilename());
      System.out.println(message.getBody().getClass());
      System.out.println(message.getBody().getParent().getClass());
      System.out.println("*************");
      System.out.println(IOUtils.toString(((TextBody) message.getBody()).getReader()));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return statuses;
  }

  private static Message parseBody(String message) {
    return null;
  }
}
