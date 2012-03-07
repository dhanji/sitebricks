package com.google.sitebricks.mail.imap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Just swallows the expunge confirmation.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class ExpungeConfirmationExtractor implements Extractor<Void> {
  private static final Logger log = LoggerFactory.getLogger(ExpungeConfirmationExtractor.class);

  @Override
  public Void extract(List<String> messages) {
    for (String message : messages) {
      log.trace("Confirmation: {}", message);
    }
    return null;
  }
}
