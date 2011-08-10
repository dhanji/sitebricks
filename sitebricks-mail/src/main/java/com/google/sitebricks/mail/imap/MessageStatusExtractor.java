package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

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
      message = message.replaceFirst("\\d+[ ]+FETCH ", "");
      statuses.add(parseEnvelope(message));
    }

    return statuses;
  }

  private static MessageStatus parseEnvelope(String message) {
    Queue<String> tokens = tokenize(message);

    // Assert that we have an envelope.
    eat(tokens, "(", "ENVELOPE", "(");

    MessageStatus status = new MessageStatus();
    String receivedDate = tokens.peek();
    if (isValid(receivedDate)) {
      receivedDate = match(tokens, String.class);
      status.setReceivedDate(RECEIVED_DATE.parseDateTime(receivedDate).toDate());
    }

    status.setSubject(match(tokens, String.class));

    status.setFrom(readAddresses(tokens));
    status.setSender(readAddresses(tokens));
    status.setReplyTo(readAddresses(tokens));
    status.setTo(readAddresses(tokens));
    status.setCc(readAddresses(tokens));
    status.setBcc(readAddresses(tokens));

    status.setInReplyTo(match(tokens, String.class));
    status.setMessageUid(match(tokens, String.class));

    // Close envelope, and open flags...
    eat(tokens, ")", "FLAGS", "(");

    // Check if there are flags to set.
    while (!")".equals(tokens.peek())) {
      status.getFlags().add(Flag.parse(tokens.poll()));
    }
    eat(tokens, ")", "INTERNALDATE");

    String internalDate = tokens.peek();
    if (isValid(internalDate)) {
      internalDate = match(tokens, String.class);
      status.setInternalDate(INTERNAL_DATE.parseDateTime(internalDate).toDate());
    }

    eat(tokens, "RFC822.SIZE");
    status.setSize(match(tokens, int.class));

    // We don't really need to bother closing the last ')'

    return status;
  }

  private static List<String> readAddresses(Queue<String> tokens) {
    if (isValid(tokens.peek())) {
      eat(tokens, "(");
      List<String> addresses = Lists.newArrayList();

      while ("(".equals(tokens.peek()))
        addresses.add(readAddress(tokens));

      eat(tokens, ")");
      return addresses;
    }
    tokens.poll();  // Discard 'NIL'
    return null;
  }

  private static String readAddress(Queue<String> tokens) {// := ( a b c d )
    StringBuilder address = new StringBuilder();
    eat(tokens, "(");
    String namePiece = match(tokens, String.class);
    String sourceRoute = match(tokens, String.class);
    String mailboxName = match(tokens, String.class);  // mail username
    String hostname = match(tokens, String.class);     // domain
    eat(tokens, ")");

    if (namePiece != null)
      address.append('"').append(namePiece).append("\" ");

    // I duno what source route is for ...
    return address.append(mailboxName).append('@').append(hostname).toString();
  }

  @SuppressWarnings("unchecked")
  private static <T> T match(Queue<String> tokens, Class<T> clazz) {
    String token = tokens.poll();
    if (!isValid(token))
      return null;

    if (String.class == clazz) {
      if (token.startsWith("\"") && token.endsWith("\""))
        return (T)token.substring(1, token.length() - 1);
      else
        throw new IllegalArgumentException("Expected a string but found: " + token);
    } else if (int.class == clazz) {
      return (T) Integer.valueOf(token);
    }
    throw new IllegalArgumentException("Unsupported type: " + clazz.getName());
  }

  private static String matchAnyOf(Queue<String> tokens, String... match) {
    for (String piece : match) {
      if (piece.equals(tokens.peek())) {
        return tokens.poll();
      }
    }

    // None found.
    return null;
  }

  private static void eat(Queue<String> tokens, String... match) {
    for (String piece : match) {
      if (piece.equals(tokens.peek())) {
        tokens.poll();
      } else
        throw new IllegalArgumentException("Expected token " + piece + " but found " + tokens.peek());
    }
  }

  private static boolean isValid(String token) {
    return !"NIL".equalsIgnoreCase(token);
  }

  static Queue<String> tokenize(String message) {
    Queue<String> tokens = new LinkedBlockingQueue<String>();

    char[] charArray = message.toCharArray();
    boolean inString = false;
    StringBuilder currentToken = new StringBuilder();
    for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
      char c = charArray[i];

      if (c == '"') {
        if (inString) {
          inString = false;

          // Bake string token.
          currentToken.append('"');
          bakeToken(tokens, currentToken);
          currentToken = new StringBuilder();
        } else {
          inString = true;
          // We've entered a string, so bake whatever has come so far.
          bakeToken(tokens, currentToken);
          currentToken = new StringBuilder();
          currentToken.append('"');
        }
        continue;

        // Handle parentheses as their own tokens.
      }

      if (!inString)
        if (c == '(') {
          bakeToken(tokens, currentToken);
          tokens.add("(");
          currentToken = new StringBuilder();
          continue;
        } else if (c == ')') {
          bakeToken(tokens, currentToken);
          tokens.add(")");
          currentToken = new StringBuilder();
          continue;

          // Otherwise whitespace is a delimiter for non-strings.
        } else if (c == ' ') {
          bakeToken(tokens, currentToken);
          currentToken = new StringBuilder();
          continue;
        }

      currentToken.append(c);
    }

    // Close up dangling tokens.
    if (currentToken.length() > 0) {
      bakeToken(tokens, currentToken);
    }

    return tokens;
  }

  private static void bakeToken(Collection<String> tokens, StringBuilder currentToken) {
    String trim = currentToken.toString().trim();
    if (trim.length() > 0)
      tokens.add(trim);
  }
}
