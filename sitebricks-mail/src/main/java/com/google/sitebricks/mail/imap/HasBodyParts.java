package com.google.sitebricks.mail.imap;

import com.google.common.collect.Multimap;

import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface HasBodyParts {
  List<Message.BodyPart> getBodyParts();

  Multimap<String, String> getHeaders();

  void setBody(String body);
  void setBody(byte[] body);
}
