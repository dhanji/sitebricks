package com.google.sitebricks.mail.imap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a complete IMAP message with all body parts materialized
 * and decoded as appropriate (for example, non-UTF8 encodings are re-encoded
 * into UTF8 for raw and rich text).
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Message implements HasBodyParts {
  private MessageStatus status;
  private Map<String, String> headers = new HashMap<String, String>();
  private List<BodyPart> bodyParts = new ArrayList<BodyPart>();

  public MessageStatus getStatus() {
    return status;
  }

  public void setStatus(MessageStatus status) {
    this.status = status;
  }
  public Map<String, String> getHeaders() {
    return headers;
  }

  public List<BodyPart> getBodyParts() {
    return bodyParts;
  }

  // Short hand.
  @Override public void setBody(String body) {
    assert bodyParts.isEmpty() : "Unexpected set body call to a multipart email";
    bodyParts.add(new BodyPart(body));
  }

  @Override public void setBody(byte[] body) {
    assert bodyParts.isEmpty() : "Unexpected set body call to a multipart email";
    bodyParts.add(new BodyPart(body));
  }

  public static class BodyPart implements HasBodyParts {
    private Map<String, String> headers = new HashMap<String, String>();

    // This field is set for HTML or text emails. and is mutually exclusive with binBody.
    private String body;

    // This field is set for all binary attachment and body types.
    private byte[] binBody;

    private List<BodyPart> bodyParts;

    public BodyPart(String body) {
      this.body = body;
    }

    public BodyPart() {
    }

    public BodyPart(byte[] body) {
      this.binBody = body;
    }

    public List<BodyPart> getBodyParts() {
      return bodyParts;
    }

    public void setBodyParts(List<BodyPart> bodyParts) {
      this.bodyParts = bodyParts;
    }

    public Map<String, String> getHeaders() {
      return headers;
    }

    public String getBody() {
      return body;
    }

    public void setBody(String body) {
      this.body = body;
    }

    public byte[] getBinBody() {
      return binBody;
    }

    public void setBody(byte[] binBody) {
      this.binBody = binBody;
    }
  }
}
