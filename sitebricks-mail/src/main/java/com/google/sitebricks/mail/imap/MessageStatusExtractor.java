package com.google.sitebricks.mail.imap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
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
  static final Pattern HELPFUL_NOTIFICATION_PATTERN = Pattern.compile("[*] \\d+ (EXISTS|EXPUNGE)\\s*",
      Pattern.CASE_INSENSITIVE);
  static final Pattern SIZE_MARKER = Pattern.compile("\\{(\\d+)\\}$", Pattern.MULTILINE);

  @Override
  public List<MessageStatus> extract(List<String> messages) {
    List<MessageStatus> statuses = Lists.newArrayList();
    for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
      String message = messages.get(i);
      if (null == message || message.isEmpty())
        continue;

      // Discard the success token and any EXISTS or EXPUNGE tokens.
      try {
        if (Command.isEndOfSequence(message) || HELPFUL_NOTIFICATION_PATTERN.matcher(message).matches())
          continue;
      } catch (ExtractionException ee) {
        log.error("Warning: error parsing email message status! {}", messages, ee);
        continue;
      }

      // The only newlines allowed are inside strings, so check whether the message
      // might have been split between lines and unfold as appropriate.
      boolean isUnterminatedString = isUnterminatedString(message, false);
      while (isUnterminatedString && (i + 1 < messagesSize)) {
        String next = messages.get(i + 1);
        message = message + '\n' + next;
        isUnterminatedString = isUnterminatedString(next, isUnterminatedString);
        // Skip next.
        i++;
      }

      // Newlines are actually also allowed outside strings if a length marker is specified.
      Matcher matcher = SIZE_MARKER.matcher(message);
      while (matcher.find()) {
        int size = Integer.parseInt(matcher.group(1));
        StringBuilder stringToken = new StringBuilder("\n");
        String rest = "";
        int newlines = 1;
        boolean done = false;
        while (stringToken.length() <= size + 1 && !done && (i + 1 < messagesSize)) {
          String next = messages.get(i + 1).trim();
          ++i;
          if (next.length() + stringToken.length() <= size) {
            stringToken.append(next).append('\n');
            newlines++;
          } else {
            int offset = Math.max(0, size - stringToken.length() - newlines * 2);
            stringToken.append(next.substring(0, offset));
            rest = next.substring(offset);

            // We could have over-counted as newlines are not always counted as 2 characters.
            // For sanity
            int bracketPos = rest.indexOf("((");
            if (bracketPos > 0 && rest.charAt(bracketPos - 1) == ' ')
              bracketPos--;

            int nilPos = rest.indexOf(" NIL");
            int delim = Math.min(bracketPos == -1 ? Integer.MAX_VALUE : bracketPos,
                nilPos == -1 ? Integer.MAX_VALUE : nilPos);
            
            if (delim == Integer.MAX_VALUE) {
              int spacePos = rest.indexOf(" ");
              delim = spacePos == -1 ? Integer.MAX_VALUE : spacePos;
            }
            
            if (delim > 0 && delim != Integer.MAX_VALUE) {
              stringToken.append(rest.substring(0, delim));
              rest = rest.substring(delim);
              done = true;
            }
          }
        }

        // Now take the extracted subject and compose it into the message header as though it
        // were quoted.
        message = matcher.replaceAll("");
        // Escape nested quotes:
        String newToken = stringToken.toString().replaceAll("\"", "\\\\\"");
        message += '"' + newToken + '"' + rest;
        // The new message string might have further size markers.
        matcher = SIZE_MARKER.matcher(message);
      }

      statuses.add(parseStatus(message.replaceFirst("^[*] ", "")));
    }

    return statuses;
  }

  /**
   Check for string termination, will check for quote escaping, but only if it's escaped
   within a string... otherwise it's illegal and we'll treat it as a regular quote.
   A trailing backslash indicates a \CRLF was received (as envisaged in RFC 822 3.4.5).
   */
  @VisibleForTesting
  static boolean isUnterminatedString(String message, boolean alreadyInString) {
    boolean escaped = false;
    boolean inString = alreadyInString;
    for (int i = 0; i < message.length(); i++) {
      final char c = message.charAt(i);
      if (inString) {
        if (c == '\\') {
          escaped = !escaped;
        } else if (c == '"') {
          if (!escaped)
            inString = false;
          escaped = false;
        } else
          escaped = false;
      } else
        inString = c == '"';
    }
    return inString;
  }

  private static MessageStatus parseStatus(String message) {
    Queue<String> tokens = Parsing.tokenize(message);
    MessageStatus status = new MessageStatus();

    try {
      // Assert that we have an envelope.
      Parsing.match(tokens, int.class);
      Parsing.eat(tokens, "FETCH", "(");

      while (!tokens.isEmpty()) {
        boolean match = parseUid(tokens, status);
        match |= parseEnvelope(tokens, status);
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
    } catch (IllegalArgumentException e) {
      log.warn("Error parsing status: {}", message);
      throw e;
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
    Set<Flag> flags = Flag.parseFlagList(tokens);
    if (flags == null)
      return false;
    status.getFlags().addAll(flags);
    return true;
  }

  private static boolean parseUid(Queue<String> tokens, MessageStatus status) {
    if (Parsing.matchAnyOf(tokens, "UID") == null)
      return false;
    status.setImapUid(Parsing.match(tokens, int.class));
    return true;
  }

  private static boolean parseGmailLabels(Queue<String> tokens, MessageStatus status) {
    if (Parsing.matchAnyOf(tokens, "X-GM-LABELS") == null)
      return false;
    Parsing.eat(tokens, "(");

    // Create a label set for this message status only if there are labels.
    status.setLabels(Sets.<String>newHashSet());

    // Check if there are labels to add.
    while (!")".equals(tokens.peek())) { // \Inbox
      String token = tokens.poll();

      // HACK: horrible hack!!!
      // The original Parser incorrectly left escaped backslashes intact. We now
      // emulate this by putting them back in...
      // this code replaces all single backslashes (escaped here as "\\\\") to double backslashes.
      token = token.replaceAll("\\\\", "\\\\\\\\");

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
    } else if (receivedDate != null) {
      Parsing.eat(tokens, "NIL");
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
