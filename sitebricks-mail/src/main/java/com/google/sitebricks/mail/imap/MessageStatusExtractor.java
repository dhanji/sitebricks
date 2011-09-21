package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

/**
 * Extracts a MessageStatus from a partial IMAP fetch. Specifically
 * a "fetch all" command which comes back with subject, sender, uid
 * internaldate and rfc822.size (length).
 * <p>
 * A more robust form of fetch exists for message body parts which
 * would handle email body, html mail, attachments, etc.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MessageStatusExtractor implements Extractor<List<MessageStatus>> {
  private static final Logger log = LoggerFactory.getLogger(MessageStatusExtractor.class);
  // 10 Sep 2011 14:19:55 -0700
  static final Pattern ALTERNATE_RECEIVED_DATE_PATTERN = Pattern.compile(
      "\\d?\\d \\w\\w\\w [0-9]{4} \\d?\\d:\\d?\\d:\\d?\\d [-+]?[0-9]{4}");

  // 10 Sep 11 14:19:55 -0700
  static final Pattern ALTERNATE_RECEIVED_DATE_PATTERN_2 = Pattern.compile(
      "\\d?\\d \\w\\w\\w [0-9]{2} \\d?\\d:\\d?\\d:\\d?\\d [-+]?[0-9]{4}");

  // A shorter form of the received date
  static final DateTimeFormatter ALT_RECEIVED_DATE = DateTimeFormat.forPattern(
      "dd MMM yyyy HH:mm:ss Z");
  static final DateTimeFormatter ALT_RECEIVED_DATE_2 = DateTimeFormat.forPattern(
      "dd MMM yy HH:mm:ss Z");
  static final DateTimeFormatter RECEIVED_DATE = DateTimeFormat.forPattern(
      "EEE, dd MMM yyyy HH:mm:ss Z");
  static final DateTimeFormatter INTERNAL_DATE = DateTimeFormat.forPattern(
      "dd-MMM-yyyy HH:mm:ss Z");

  @Override
  public List<MessageStatus> extract(List<String> messages) {
    List<MessageStatus> statuses = Lists.newArrayList();
    for (String message : messages) {
      if (null == message || message.isEmpty())
        continue;

      // Discard the fetch token.
      message = message.replaceFirst("[*]?[ ]*\\d+[ ]* ", "");
      if (Command.isEndOfSequence(message.toLowerCase()))
        continue;
      statuses.add(parseEnvelope(message));
    }

    return statuses;
  }

  private static MessageStatus parseEnvelope(String message) {
    Queue<String> tokens = Parsing.tokenize(message);

    // Assert that we have an envelope.
    Parsing.eat(tokens, "FETCH", "(", "ENVELOPE", "(");

    MessageStatus status = new MessageStatus();
    String receivedDate = tokens.peek();
    if (Parsing.isValid(receivedDate)) {
      receivedDate = Parsing.normalizeDateToken(Parsing.match(tokens, String.class));
      try {
        status.setReceivedDate(new javax.mail.internet.MailDateFormat().parse(receivedDate));
      } catch (ParseException e) {
        log.error("Malformed received date format {}. Unable to parse.", receivedDate, e);
      }
    }

    status.setSubject(Parsing.match(tokens, String.class));

    status.setFrom(Parsing.readAddresses(tokens));
    status.setSender(Parsing.readAddresses(tokens));
    status.setReplyTo(Parsing.readAddresses(tokens));
    status.setTo(Parsing.readAddresses(tokens));
    status.setCc(Parsing.readAddresses(tokens));
    status.setBcc(Parsing.readAddresses(tokens));

    status.setInReplyTo(Parsing.match(tokens, String.class));
    status.setMessageUid(Parsing.match(tokens, String.class));

    // Close envelope, and open flags...
    Parsing.eat(tokens, ")", "FLAGS", "(");

    // Check if there are flags to set.
    while (!")".equals(tokens.peek())) {
      String token = tokens.poll();
      Flag flag = Flag.parse(token);
      if (flag != null)
        status.getFlags().add(flag);
      else log.warn("Unknown flag type encountered {}, ignoring.", token);
    }
    Parsing.eat(tokens, ")", "INTERNALDATE");

    String internalDate = tokens.peek();
    if (Parsing.isValid(internalDate)) {
      internalDate = Parsing.normalizeDateToken(Parsing.match(tokens, String.class));
      status.setInternalDate(INTERNAL_DATE.parseDateTime(internalDate).toDate());
    }

    Parsing.eat(tokens, "RFC822.SIZE");
    status.setSize(Parsing.match(tokens, int.class));

    // We don't really need to bother closing the last ')'

    return status;
  }
}
