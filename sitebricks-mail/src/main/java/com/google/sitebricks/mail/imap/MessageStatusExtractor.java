package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

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
  private static final String ENVELOPE_PREFIX = "(ENVELOPE ";
  private static final String INTERNALDATE = "INTERNALDATE";

  @Override
  public List<MessageStatus> extract(List<String> messages) {
    List<MessageStatus> statuses = Lists.newArrayList();
    for (String message : messages) {
      String[] split = message.split("[ ]+", 3);

      // Only parse Fetch responses.
      if (split.length > 1 && "FETCH".equalsIgnoreCase(split[1])) {
        // Strip the "XX FETCH" sequence prefix first.
        statuses.add(parseEnvelope(split[2]));
      }
    }

    return statuses;
  }

  private static MessageStatus parseEnvelope(String message) {
    // Now we have only the envelope remaining.
    if (!message.startsWith(ENVELOPE_PREFIX)) {
      // Something's wrong, we can't handle this.
      throw new RuntimeException("Illegal data format, expecting envelope prefix, " +
          "found " + message);
    }

    // Strip envelope wrapper.
    message = message.substring(ENVELOPE_PREFIX.length(), message.length() - 1);

    // Parse strings or paren-groups.
    List<String> tokens = tokenize(message);

    // Parse semantic message information out of the token stream.

    // First piece is always the received date.
    String receivedDateRaw = tokens.get(0);
    String subject = tokens.get(1);
    String senderName = tokens.get(2);

    // sender/recipient addresses are a bit funky.

    // Skip ahead to last-but-one (message uid).
    String messageUidRaw = tokens.get(tokens.size() - 2);
    String messageUid = messageUidRaw.substring(messageUidRaw.indexOf('<'),
        messageUidRaw.length() - 1);

    // Last token is the combined Flags and internaldate token.
    EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
    String last = tokens.get(tokens.size() - 1);
    for (String fragment : last.split("[ ]+")) {
      if (fragment.startsWith("\\")) {
        // This is an IMAP flag. Do something with it.
        Flag flag = Flag.named(fragment.substring(1).toUpperCase());
        if (null != flag)
          flags.add(flag);
        // Else maybe log warning that we encountered an unknown flag?
      }

      // Are we done processing flags?
      if (INTERNALDATE.equalsIgnoreCase(fragment)) {
        break;
      }
    }

    last = last.substring(last.indexOf(INTERNALDATE) + INTERNALDATE.length() + 1);

    // Parse date from this final piece.
    Date internalDate, receivedDate;
    try {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss ZZZZZ");
      internalDate = dateFormat.parse(last);
//      receivedDate = dateFormat.parse(receivedDateRaw);

    } catch (ParseException e) {
      throw new RuntimeException("Unable to parse date from " + last, e);
    }

    return new MessageStatus(messageUid, internalDate, subject, flags);
  }

  private static List<String> tokenize(String message) {
    List<String> pieces = Lists.newArrayList();
    char[] chars = message.toCharArray();
    boolean inString = false;
    StringBuilder token = new StringBuilder();
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (c == '"') {

        // Close of string, bake this token.
        if (inString) {
          pieces.add(token.toString().trim());
          token = new StringBuilder();
          inString = false;
        } else
          inString = true;

        continue;
      }

      // Skip parentheticals
      if (!inString && (c == '(' || c == ')')) {
        continue;
      }

      token.append(c);
    }
    return pieces;
  }
}
