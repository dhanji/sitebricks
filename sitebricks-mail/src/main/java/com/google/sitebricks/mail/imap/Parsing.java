package com.google.sitebricks.mail.imap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.codec.DecoderUtil;

import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Parsing {

  static List<String> readAddresses(Queue<String> tokens) {
    // Weird base case where we don't get nil, but instead get an empty address set.
    if (tokens.isEmpty())
      return ImmutableList.of();
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
    if (namePiece != null)
      namePiece = namePiece.replace("\\", "");

    String sourceRoute = match(tokens, String.class);
    String mailboxName = match(tokens, String.class);  // mail username
    String hostname = match(tokens, String.class);     // domain
    eat(tokens, ")");

    if (namePiece != null)
      address.append('"').append(decode(namePiece)).append("\" ");

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
    } else if (long.class == clazz) {
      return (T) Long.valueOf(token);
    }
    throw new IllegalArgumentException("Unsupported type: " + clazz.getName());
  }

  static String matchAnyOf(Queue<String> tokens, String... match) {
    for (String piece : match) {
      if (piece.equalsIgnoreCase(tokens.peek())) {
        return tokens.poll();
      }
    }

    // None found.
    return null;
  }

  static void eat(Queue<String> tokens, String... match) {
    for (String piece : match) {
      if (piece.equalsIgnoreCase(tokens.peek())) {
        tokens.poll();
      } else
        throw new IllegalArgumentException("Expected token " + piece + " but found "
            + tokens.peek() +" in [" + tokens + "]");
    }
  }

  static boolean isValid(String token) {
    return null != token && !"NIL".equalsIgnoreCase(token);
  }

  static String normalizeDateToken(String token) {
    return token.replaceAll(" \\(.+\\)$", "").replaceAll("[ ]+", " ").trim();
  }

  static Queue<String> tokenize(String message) {
    Queue<String> tokens = new LinkedBlockingQueue<String>();

    char[] charArray = message.toCharArray();
    boolean inString = false;
    StringBuilder currentToken = new StringBuilder();
    boolean escaped = false;
    for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
      char c = charArray[i];
      if (c == '\\') {
        if (escaped) { // i.e. two backlashes in a row..
          currentToken.append('\\');
          escaped = false;
        } else {
          escaped = true;
        }
        continue;
      }

      // String checks, but only if we're not an escaped quote character.
      if (c == '"' && !escaped) {
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
      }

      if (!inString) {
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

          // Otherwise whitespace is a delimiter for non-strings. EXCEPT when
          // preceeded by '\', which is an escape character.
        } else if (c == ' ' && !escaped) {
          bakeToken(tokens, currentToken);
          currentToken = new StringBuilder();
          continue;
        }
      }

      // Only swallow backslashes if this character was escaped inside a string.
      if (escaped && !inString) {
        currentToken.append('\\');
      }
      currentToken.append(c);
      escaped = false;
    }

    // Close up dangling tokens.
    bakeToken(tokens, currentToken);

    return tokens;
  }

  static void bakeToken(Collection<String> tokens, StringBuilder currentToken) {
    String trim = currentToken.toString().trim();
    if (trim.length() > 0)
      tokens.add(trim);
  }

  public static boolean startsWithIgnoreCase(String toTest, String prefix) {
    if (null == toTest)
      return (null == prefix);

    return toTest.toLowerCase().startsWith(prefix.toLowerCase());
  }

  public static String stripQuotes(String var) {
    if (var.startsWith("\"") && var.endsWith("\""))
      return var.substring(1, var.length() - 1);
    return var;
  }

  public static String decode(String str) {
    // decode as per http://www.ietf.org/rfc/rfc2047.txt
    return str == null
        ? null
        : str.isEmpty()
            ? str
            : DecoderUtil.decodeEncodedWords(str, DecodeMonitor.SILENT);
  }

  public static Collection<String> getKeyVariations(Multimap<String, String> headers, String... keys) {
    for (String key : keys) {
      Collection<String> values = headers.get(key);
      if (!values.isEmpty())
        return values;
    }
    return ImmutableList.of();
  }
}
