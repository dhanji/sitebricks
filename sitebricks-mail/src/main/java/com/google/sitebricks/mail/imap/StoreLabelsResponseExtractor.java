package com.google.sitebricks.mail.imap;

import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse STORE_LABELS response, including new flags as echoed by server.
 * TODO: time allowing, generify and merge with StoreFlagsResponseExtractor.
 * TODO: replace "STORE_FLAGS RESPONSE" with an error code or string for easy classification.
 *
 * @author jochen@pedesis.org (Jochen Bekmann)
 */
public class StoreLabelsResponseExtractor implements Extractor<Set<String>> {
  private static final Pattern FETCH_LABEL_PATT = Pattern.compile(".* FETCH *\\(X-GM-LABELS( *\\(.*\\))" +
      ".*\\)");
  private static final Pattern BAD_PATT = Pattern.compile("\\d+ +BAD (.*)");
  private static final Pattern NO_PATT = Pattern.compile("\\d+ +NO (.*)");
  private static final Pattern OK_PATT = Pattern.compile("\\d+ +OK (.*)");

  /**
   * Parse the response, which includes label settings and command status.
   * We expect only one FETCH response as we only set labels on one msg.
   *
   * http://code.google.com/apis/gmail/imap/#x-gm-labels
   *  C: a011 STORE_FLAGS 1 +X-GM-LABELS (foo)
   *  S: * 1 FETCH (X-GM-LABELS (\Inbox \Sent Important "Muy Importante" foo))
   *  S: a011 OK STORE_FLAGS (Success)
   */
  @Override
  public Set<String> extract(List<String> messages) throws ExtractionException {
    boolean gotFetch = false;
    Set<String> result = null;

    // Find FETCH, throw error if none or more than one, or if we receive an error response.
    String fetchStr = null;
    for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
      String message = messages.get(i);
      if (null == message || message.isEmpty())
        continue;

      fetchStr = matchAndGetGroup1(FETCH_LABEL_PATT, message);
      if(fetchStr != null) {
        if (gotFetch) {
          throw new ExtractionException("STORE_LABELS RESPONSE: Got more than one FETCH " +
              "response " + message);
        }
        gotFetch = true;
        result = Sets.<String>newHashSet();
        result.addAll(Parsing.tokenize(fetchStr));
        result.remove("(");
        result.remove(")");
      } else {
        if(matchAndGetGroup1(OK_PATT, message) != null) {
          if (!gotFetch) {
            throw new ExtractionException("STORE_LABELS RESPONSE: no LABELS received." +
                message);
          }
          // All Good.
        } else if (matchAndGetGroup1(BAD_PATT, message) != null || matchAndGetGroup1(NO_PATT,
            message) != null) {
          throw new ExtractionException("STORE_LABELS RESPONSE: " + message);
        }
      }
    }
    return result;
  }

  private String matchAndGetGroup1(Pattern p, String s) {
    Matcher m = p.matcher(s);

    if (m.matches() && m.groupCount() > 0) {
      return m.group(1);
    }
    return null;
  }
}
