package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Extracts a Message from a complete IMAP fetch. Specifically
 * a "fetch full" command which comes back with subject, sender, uid
 * internaldate and rfc822.size (length) and all body parts.
 * <p>
 * This is the more robust form of {@link MessageStatus}, which should
 * be preferred for fetching just subjects/status info as this extraction
 * mechanism can be slower for messages with large bodies.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MessageExtractor implements Extractor<List<MessageStatus>> {
  private static final String ENVELOPE_PREFIX = "(ENVELOPE ";
  private static final String INTERNALDATE = "INTERNALDATE";

  @Override
  public List<MessageStatus> extract(List<String> messages) {
    List<MessageStatus> statuses = Lists.newArrayList();
    for (String message : messages) {
      System.out.println(message);
      String[] split = message.split("[ ]+", 3);

      // Only parse Fetch responses.
      if (split.length > 1 && "FETCH".equalsIgnoreCase(split[1])) {
        // Strip the "XX FETCH" sequence prefix first.
//        statuses.add(parseEnvelope(split[2]));
      }
    }

    return statuses;
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
