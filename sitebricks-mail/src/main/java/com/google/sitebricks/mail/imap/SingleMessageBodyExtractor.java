package com.google.sitebricks.mail.imap;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class SingleMessageBodyExtractor implements Extractor<Message> {
  private final MessageBodyExtractor extractor = new MessageBodyExtractor();

  @Override public Message extract(List<String> messages) throws ExtractionException {
    List<Message> extract = extractor.extract(messages);

    return extract.isEmpty() ? null : extract.iterator().next();
  }
}
