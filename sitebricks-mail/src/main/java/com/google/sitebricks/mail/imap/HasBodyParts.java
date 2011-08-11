package com.google.sitebricks.mail.imap;

import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface HasBodyParts {
  List<Message.BodyPart> getBodyParts();

  Map<String, String> getHeaders();

  void setBody(String body);
  void setBody(byte[] body);
}
