package com.google.sitebricks.mail.imap;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Extracts a set of integers (UIDs) from a search result.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class SearchResultExtractor implements Extractor<List<Integer>> {
  private static final Logger log = LoggerFactory.getLogger(SearchResultExtractor.class);

  @Override
  public List<Integer> extract(List<String> messages) {
    List<Integer> uids = null;
    for (int i = 0, messagesSize = messages.size(); i < messagesSize; i++) {
      String message = messages.get(i);
      if (null == message || message.isEmpty())
        continue;

      // Discard the success token and any EXISTS or EXPUNGE tokens.
      try {
        if (Command.isEndOfSequence(message)
            || MessageStatusExtractor.HELPFUL_NOTIFICATION_PATTERN.matcher(message).matches())
          continue;
      } catch (ExtractionException ee) {
        log.error("Warning: error parsing search results! {}", messages, ee);
        continue;
      }

      message = message.substring("* search".length());
      if (message.trim().isEmpty())
        continue;

      if (uids == null)
        uids = Lists.newArrayList();
      for (String piece : message.split("[ ]+")) {
        try {
          uids.add(Integer.valueOf(piece));
        } catch (NumberFormatException nfe) {
          log.error("Unable to parse search result {}", message, nfe);
        }
      }
    }

    return uids;
  }
}
