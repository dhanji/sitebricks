package com.google.sitebricks.compiler;


import com.google.sitebricks.rendering.Strings;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Utility tokenizes text into expressions and raw text, and provides other
 * text parsing tools.
 *
 * @author Dhanji R. Prasanna (dhanji at gmail com)
 * @since 1.0
 */
public class Parsing {

  private Parsing() {
  }


  //converts comma-separated name/value pairs into expression/variable bindings
  public static Map<String, String> toBindMap(String expression) {
    if (Strings.empty(expression))
      return Collections.emptyMap();


    boolean escape = false;
    List<String> pairs = new ArrayList<String>();
    int index = 0;
    for (int i = 0; i < expression.length(); i++) {
      char c = expression.charAt(i);

      //skip commas in strings
      if ('"' == c)
        escape = !escape;

      if (!escape && ',' == c) {
        if (index < i)
          pairs.add(expression.substring(index, i));

        //skip comma & whitespace if any
        for (; i < expression.length() && (',' == expression.charAt(i) || ' ' == expression.charAt(i));)
          i++;

        //reset new start index
        index = i;
      }

    }

    //add last pair if needed
    if (index < expression.length()) {

      //chew up leading comma & whitespace if any
      //noinspection StatementWithEmptyBody
      for (; ',' == expression.charAt(index) || ' ' == expression.charAt(index); index++) ;

      final String pair = expression.substring(index, expression.length()).trim();

      //only consider this a pair if it has something in it!
      if (pair.length() > 1)
        pairs.add(pair);
    }

    //nice to preserve insertion order
    final Map<String, String> map = new LinkedHashMap<String, String>();
    for (String pair : pairs) {
      final String[] nameAndValue = pair.split("=", 2);

      //do some validation
      if (nameAndValue.length != 2)
        throw new IllegalArgumentException("Invalid parameter binding format: " + pair);

      Strings.nonEmpty(nameAndValue[0], "Cannot have an empty left hand side target parameter: " + pair);
      Strings.nonEmpty(nameAndValue[1], "Must provide a non-empty right hand side expression: " + pair);

      map.put(nameAndValue[0].trim(), nameAndValue[1].trim());
    }

    return Collections.unmodifiableMap(map);
  }


  //tokenizes text into raw text chunks interspersed with expression chunks
  public static List<Token> tokenize(String warpRawText, EvaluatorCompiler compiler) throws ExpressionCompileException {
    ArrayList<Token> tokens = new ArrayList<Token>();

    //simple state machine to iterate the text and break it up into chunks
    char[] characters = warpRawText.toCharArray();

    StringBuilder token = new StringBuilder();
    TokenizerState state = TokenizerState.READING_TEXT;
    for (int i = 0; i < characters.length; i++) {

      //test for start of an expression
      if (TokenizerState.READING_TEXT.equals(state)) {
        if ('$' == characters[i]) {
          if ('{' == characters[i + 1]) {
            //YES it is the start of an expr, so close up the existing token & start a new one
            if (token.length() > 0) {
              tokens.add(CompiledToken.text(token.toString()));
              token = new StringBuilder();
            }

            state = TokenizerState.READING_EXPRESSION;
          }
        }
      }

      //test for end of an expr
      if (TokenizerState.READING_EXPRESSION.equals(state)) {
        if ('}' == characters[i]) {
          //YES it is the end of the expr, so close it up and start a new token
          token.append(characters[i]);

          tokens.add(CompiledToken.expression(token.toString(), compiler));
          token = new StringBuilder();

          state = TokenizerState.READING_TEXT;
          continue;   //dont add the trailing } to the new text field
        }
      }

      //add characters to the token normally
      token.append(characters[i]);
    }

    //should never be in reading expr mode at this point
    if (TokenizerState.READING_EXPRESSION.equals(state))
      throw new IllegalStateException("Error. Expression was not terminated properly: " + token.toString());

    //add last token read if it has any content (is always text)
    if (token.length() > 0)
      tokens.add(CompiledToken.text(token.toString()));

    // Pack list capacity to size (saves memory).
    tokens.trimToSize();

    return tokens;
  }

  public static String stripExpression(String expr) {
    return expr.substring(2, expr.length() - 1);
  }

  public static boolean isExpression(String attribute) {
    return attribute.startsWith("${");
  }


  //dont pass null or empty string or 1 char
  public static String stripQuotes(String var) {
    return var.substring(1, var.length() - 1);
  }

  /**
   * Remember this method is not so much about verifying something is XML as it is
   * verifying that something is a NON-Xml template. In other words, read this as
   * whether or not we should *treat* something as XML, then complain that it's malformed
   * later (if necessary).
   *
   * @param template A fully loaded template as a string.
   * @return Returns true if this template should be treated as an XML template.
   *         Templates that are not XML *MUST* begin with a {@code @Meta} annotation.
   */
  public static boolean treatAsXml(String template) {
    return 0 > indexOfMeta(template);
  }

  /**
   * Converts the given token stream into a rendered output evaluating each expression
   * against the provided context object which may be a regular Java POJO with getters
   * and setters or a map of string/value pairs.
   */
  public static String render(List<Token> tokens, Map<String, Object> arguments) {
    StringBuilder builder = new StringBuilder();
    for (Token token : tokens) {
        builder.append(token.render(arguments));
    }

    return builder.toString();
  }

  public static int indexOfMeta(String template) {
    //do a manual character scan (coz indexOf(regex) will be O(n) runtime)
    for (int i = 0; i < template.length(); i++) {
      char c = template.charAt(i);

      //skip leading whitespace
      if (isWhitespace(c))
        continue;

      //Does this template begin with @Meta or @Meta(  --> then it is *not* XML
      if ('@' == c) {
        final char trailing = template.charAt(i + 5);

        if ("Meta".equals(template.substring(i + 1, i + 5))

            && ('(' == trailing || isWhitespace(trailing)))

          return i;
      }

      //do not go past the first non-whitespace character (short-circuit)
      return -1;
    }

    //treat everything else as XML
    return -1;
  }

  private static boolean isWhitespace(char c) {
    return ' ' == c || '\n' == c || '\r' == c || '\t' == c;
  }

  private static enum TokenizerState {
    READING_TEXT, READING_EXPRESSION
  }

  //URI test regex: (([a-zA-Z][0-9a-zA-Z+\\-\\.]*:)?/{0,2}[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?(#[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?
  //Taken from stylus studio message board http://www.stylusstudio.com/xmldev/200108/post10890.html

  private final static Pattern URI_REGEX 
      = Pattern.compile("(([a-zA-Z][0-9a-zA-Z+\\-\\.]*:)?/{0,2}[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?(#[0-9a-zA-Z;/?:@&=+$\\.\\-_!~*'()%]+)?");

//      "(([a-zA-Z][0-9a-zA-Z+\\\\-\\\\.]*:)?/{0,2}[0-9a-zA-Z;" +
//      "/?:@&=+$\\\\.\\\\-_!~*'()%]+)?(#[0-9a-zA-Z;/?:@&=+$\\\\.\\\\-_!~*'()%]+)?");

  //TODO
  private final static Pattern TEMPLATE_URI_PATTERN = Pattern.compile("(([a-zA-Z][0-9a-zA-Z+\\\\-\\\\.]*:)?/{0,2}[0-9a-zA-Z;" +
      "/?:@&=+$\\\\.\\\\-_!~*'()%]+)?(#[0-9a-zA-Z;/?:@&=+$\\\\.\\\\-_!~*'()%]+)?");


  //less expensive method tests whether string is a valid URI
  public static boolean isValidURI(String uri) {
    return (null != uri)
        && URI_REGEX
        .matcher(uri)
        .matches();
  }
}
