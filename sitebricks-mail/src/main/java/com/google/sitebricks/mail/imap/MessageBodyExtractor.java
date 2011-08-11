package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

    StringBuilder textBody = new StringBuilder();
    // Read the header message.
    String firstLine = messages.get(0);
    for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
      String message = messages.get(i);
      // Discard the fetch token.
      if (Command.isEndOfSequence(message.replaceFirst("\\d+[ ]* ", "").toLowerCase()))
        continue;

      textBody.append(message).append("\r\n");
    }

    return statuses;
  }

  private static Message parseBody(String message) {
    return null;
  }
}
