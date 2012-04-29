package com.google.sitebricks.mail.imap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Extracts a set of integers (UIDs) from a search result.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class CopyResponseExtractor implements Extractor<Boolean> {
  private static final Logger log = LoggerFactory.getLogger(CopyResponseExtractor.class);

  @Override
  public Boolean extract(List<String> messages) {
    boolean ok = false;
    for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
      String message = messages.get(i);
      if (null == message || message.isEmpty())
        continue;

      // Discard the success token and any EXISTS or EXPUNGE tokens.
      try {
        if (Parsing.startsWithIgnoreCase(message.toLowerCase(), "ok copy completed")
            || Command.OK_SUCCESS.matcher(message).matches()
            || Command.isEndOfSequence(message)
            || MessageStatusExtractor.HELPFUL_NOTIFICATION_PATTERN.matcher(message).matches()) {
          ok = true;
        }
      } catch (ExtractionException ee) {
        log.error("Warning: error parsing search results! {}", messages, ee);
        ok = false;
      }
    }

    return ok;
  }
}
