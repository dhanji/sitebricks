package com.google.sitebricks.mail.imap;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface HasBodyParts {
  List<Message.BodyPart> getBodyParts();

  void setBody(String body);
  void setBody(byte[] body);
}
