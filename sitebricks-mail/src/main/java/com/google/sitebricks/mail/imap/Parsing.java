package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class Parsing {
  static List<String> readAddresses(Queue<String> tokens) {
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

  static String readAddress(Queue<String> tokens) {
    // := ( a b c d )
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
  static <T> T match(Queue<String> tokens, Class<T> clazz) {
    String token = tokens.poll();
    if (!isValid(token))
      return null;

    if (String.class == clazz) {
      if (token.startsWith("\"") && token.endsWith("\""))
        return (T)token.substring(1, token.length() - 1);
      else
        return (T)token;
    } else if (int.class == clazz) {
      return (T) Integer.valueOf(token);
    }
    throw new IllegalArgumentException("Unsupported type: " + clazz.getName());
  }

  static String matchAnyOf(Queue<String> tokens, String... match) {
    for (String piece : match) {
      if (piece.equals(tokens.peek())) {
        return tokens.poll();
      }
    }

    // None found.
    return null;
  }

  static void eat(Queue<String> tokens, String... match) {
    for (String piece : match) {
      if (piece.equals(tokens.peek())) {
        tokens.poll();
      } else
        throw new IllegalArgumentException("Expected token " + piece + " but found " + tokens.peek());
    }
  }

  static boolean isValid(String token) {
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

  static void bakeToken(Collection<String> tokens, StringBuilder currentToken) {
    String trim = currentToken.toString().trim();
    if (trim.length() > 0)
      tokens.add(trim);
  }
}
