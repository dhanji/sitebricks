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

    // sender/recipient etc. are parsed as 4-part address structures.
    String from = parseAddress(tokens, 2);
    String sender = parseAddress(tokens, 5);
    String replyTo = "";//parseAddress(tokens, 9);

    // Skip ahead to last-but-one (message uid).
    String messageUidRaw = tokens.get(tokens.size() - 2);
    String messageUid = "";
//        messageUidRaw.substring(messageUidRaw.indexOf('<'),
//        messageUidRaw.length() - 1);

    // Last token is the combined Flags and internaldate token.
    EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
    String last = tokens.get(tokens.size() - 1);
    System.out.println(tokens);
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

    return new MessageStatus(messageUid, internalDate, subject, flags, from, sender, replyTo);
  }

  private static String parseAddress(List<String> tokens, int start) {
    StringBuilder builder = new StringBuilder();

    // Name of addressee.
    String token = tokens.get(start);
    if (isValid(token)) {
      builder.append('"');
      builder.append(token);
      builder.append("\" ");
    }

    // Build the email address itself.
    // TODO: Im not really sure what the start + 1 field is supposed to be.
    token = tokens.get(start + 1);
    builder.append(token);
    builder.append('@');
    builder.append(tokens.get(start + 2));

    return builder.toString();
  }

  private static boolean isValid(String token) {
    return !"NIL".equalsIgnoreCase(token);
  }

  private static List<String> tokenize(String message) {
    System.out.println(message);
    List<String> pieces = Lists.newArrayList();
    char[] chars = message.toCharArray();
    boolean inString = false;
    int paren = 0;
    StringBuilder token = new StringBuilder();
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];

      // Skip top-level parentheticals, but honor 2nd-level ones.
      if (!inString) {
        if (c == '(') {
          paren++;
//          if (paren < 2) { // Skip 2 levels
            continue;
//          }
          
        } else if (c == ')') {
          paren--;

          // Time to bake this as a token (skip top level).
          if (paren > 1) {
//            token.append(c);
            pieces.add(token.toString().trim());
            token = new StringBuilder();
          }
          continue;
        }
      }      

      if (c == '"' && paren < 2) {

        // Close of string, bake this token.
        if (inString) {
          pieces.add(token.toString().trim());
          token = new StringBuilder();
          inString = false;
        } else
          inString = true;

        continue;
      }

      token.append(c);
    }

    System.out.println();
    System.out.println();
    System.out.println();
    for (String piece : pieces) {
      System.out.println(piece);
    }
    return pieces;
  }
}
