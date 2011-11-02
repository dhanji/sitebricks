package com.google.sitebricks.mail.imap;

import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse STORE response, including new flags as echoed by server.
 * @author jochen@pedesis.org (Jochen Bekmann)
 */
public class StoreResponseExtractor implements Extractor<EnumSet<Flag>> {
  private static final Pattern FETCH_PATT = Pattern.compile(".* FETCH *\\((FLAGS *\\(.*\\)).*\\)");
  private static final Pattern BAD_PATT = Pattern.compile("\\d+ +BAD (.*)");
  private static final Pattern NO_PATT = Pattern.compile("\\d+ +NO (.*)");
  private static final Pattern OK_PATT = Pattern.compile("\\d+ +OK (.*)");

  /**
   * Parse the response, which includes new flag settings and command status.
   * We expect only one FETCH response as we only set flags on one msg.
   * 
   * http://tools.ietf.org/html/rfc3501#section-6.4.6
   *   C: A003 STORE 6 +FLAGS (\Deleted)
   *   S: * 4 FETCH (FLAGS (\Deleted \Flagged \Seen) UID 6)
   *   S: A003 OK STORE completed
   */
    @Override
    public EnumSet<Flag> extract(List<String> messages) throws ExtractionException {
      boolean gotFetch = false;
      EnumSet<Flag> result = EnumSet.noneOf(Flag.class);

      // Find FETCH, throw error if none or more than one, or if we receive an error response.
      String fetchStr = null;
      for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
        String message = messages.get(i);
        if (null == message || message.isEmpty())
          continue;

        fetchStr = matchAndGetGroup1(FETCH_PATT, message);
        if(fetchStr != null) {
          if (gotFetch) {
            throw new ExtractionException("STORE RESPONSE: Got more than one FETCH response " +
                message);
          }
          gotFetch = true;
          result = Flag.parseFlagList(Parsing.tokenize(fetchStr));
        } else {
          if(matchAndGetGroup1(OK_PATT, message) != null) {
            if (!gotFetch) {
              throw new ExtractionException("STORE RESPONSE: no FLAGS received." + message);
            }
            // All Good.
          } else if (matchAndGetGroup1(BAD_PATT, message) != null || matchAndGetGroup1(NO_PATT,
              message) != null) {
            throw new ExtractionException("STORE RESPONSE: " + message);
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
