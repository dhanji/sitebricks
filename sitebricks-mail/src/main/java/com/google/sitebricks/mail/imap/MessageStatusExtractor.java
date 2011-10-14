package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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

  static final DateTimeFormatter INTERNAL_DATE = DateTimeFormat.forPattern(
      "dd-MMM-yyyy HH:mm:ss Z");

  @Override
  public List<MessageStatus> extract(List<String> messages) {
    List<MessageStatus> statuses = Lists.newArrayList();
    for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
      String message = messages.get(i);
      if (null == message || message.isEmpty())
        continue;

      // Discard the success token.
      if (Command.isEndOfSequence(message))
        continue;

      // Appears that this message got split between lines. So unfold.
      if (!message.endsWith(")")) {
        String next = messages.get(i + 1);
        message = message + '\n' + next;

        // Skip next.
        i++;
      }

      statuses.add(parseStatus(message.replaceFirst("^[*] ", "")));
    }

    return statuses;
  }

  private static MessageStatus parseStatus(String message) {
    Queue<String> tokens = Parsing.tokenize(message);
    MessageStatus status = new MessageStatus();

    // Assert that we have an envelope.
    Parsing.match(tokens, int.class);
    Parsing.eat(tokens, "FETCH", "(");

    while (!tokens.isEmpty()) {
      boolean match = parseEnvelope(tokens, status);
      match |= parseFlags(tokens, status);
      match |= parseInternalDate(tokens, status);
      match |= parseRfc822Size(tokens, status);

      match |= parseGmailUid(tokens, status);
      match |= parseGmailThreadId(tokens, status);
      match |= parseGmailLabels(tokens, status);

      if (!match) {
        break;
      }
    }

    // We don't really need to bother closing the last ')'

    return status;
  }

  private static boolean parseRfc822Size(Queue<String> tokens, MessageStatus status) {
    if (Parsing.matchAnyOf(tokens, "RFC822.SIZE") == null)
      return false;
    status.setSize(Parsing.match(tokens, int.class));
    return true;
  }

  private static boolean parseGmailThreadId(Queue<String> tokens, MessageStatus status) {
    if (Parsing.matchAnyOf(tokens, "X-GM-THRID") == null)
      return false;
    status.setThreadId(Parsing.match(tokens, long.class));
    return true;
  }

  private static boolean parseGmailUid(Queue<String> tokens, MessageStatus status) {
    if (Parsing.matchAnyOf(tokens, "X-GM-MSGID") == null)
      return false;
    status.setGmailMsgId(Parsing.match(tokens, long.class));
    return true;
  }

  private static boolean parseInternalDate(Queue<String> tokens, MessageStatus status) {
    if (Parsing.matchAnyOf(tokens, "INTERNALDATE") == null)
      return false;

    String internalDate = tokens.peek();
    if (Parsing.isValid(internalDate)) {
      internalDate = Parsing.normalizeDateToken(Parsing.match(tokens, String.class));
      status.setInternalDate(INTERNAL_DATE.parseDateTime(internalDate).toDate());
    }

    return true;
  }

  private static boolean parseFlags(Queue<String> tokens, MessageStatus status) {
    if (Parsing.matchAnyOf(tokens, "FLAGS") == null)
      return false;
    Parsing.eat(tokens, "(");

    // Check if there are flags to set.
    while (!")".equals(tokens.peek())) {
      String token = tokens.poll();
      Flag flag = Flag.parse(token);
      if (flag != null)
        status.getFlags().add(flag);
      else log.warn("Unknown flag type encountered {}, ignoring.", token);
    }
    Parsing.eat(tokens, ")");
    return true;
  }

  private static boolean parseGmailLabels(Queue<String> tokens, MessageStatus status) {
    if (Parsing.matchAnyOf(tokens, "X-GM-LABELS") == null)
      return false;
    Parsing.eat(tokens, "(");

    // Create a label set for this message status only if there are labels.
    status.setLabels(Sets.<String>newHashSet());

    // Check if there are labels to add.
    while (!")".equals(tokens.peek())) {
      String token = tokens.poll();
      status.getLabels().add(token);
    }
    Parsing.eat(tokens, ")");
    return true;
  }

  private static boolean parseEnvelope(Queue<String> tokens, MessageStatus status) {
    if (Parsing.matchAnyOf(tokens, "ENVELOPE") == null)
      return false;
    Parsing.eat(tokens, "(");

    String receivedDate = tokens.peek();
    if (Parsing.isValid(receivedDate)) {
      receivedDate = Parsing.normalizeDateToken(Parsing.match(tokens, String.class));
      try {
        status.setReceivedDate(new javax.mail.internet.MailDateFormat().parse(receivedDate));
      } catch (ParseException e) {
        log.error("Malformed received date format {}. Unable to parse.", receivedDate, e);
      }
    }

    status.setSubject(Parsing.decode(Parsing.match(tokens, String.class)));

    status.setFrom(Parsing.readAddresses(tokens));
    status.setSender(Parsing.readAddresses(tokens));
    status.setReplyTo(Parsing.readAddresses(tokens));
    status.setTo(Parsing.readAddresses(tokens));
    status.setCc(Parsing.readAddresses(tokens));
    status.setBcc(Parsing.readAddresses(tokens));

    status.setInReplyTo(Parsing.match(tokens, String.class));
    status.setMessageUid(Parsing.match(tokens, String.class));

    // Close envelope.
    Parsing.eat(tokens, ")");
    return true;
  }
}
