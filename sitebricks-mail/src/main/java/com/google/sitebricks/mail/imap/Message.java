package com.google.sitebricks.mail.imap;

import com.google.common.base.Supplier;
import com.google.common.collect.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a complete IMAP message with all body parts materialized
 * and decoded as appropriate (for example, non-UTF8 encodings are re-encoded
 * into UTF8 for raw and rich text).
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Message implements HasBodyParts {
  private MessageStatus status;

  // A header can have multiple, different values.
  private Multimap<String, String> headers = newListMultimap();
  private List<BodyPart> bodyParts = new ArrayList<BodyPart>();

  public MessageStatus getStatus() {
    return status;
  }

  public void setStatus(MessageStatus status) {
    this.status = status;
  }
  public Multimap<String, String> getHeaders() {
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
    private Multimap<String, String> headers = newListMultimap();

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

    public Multimap<String, String> getHeaders() {
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

  private static ListMultimap<String, String> newListMultimap() {
    return Multimaps.newListMultimap(
        Maps.<String, Collection<String>>newLinkedHashMap(), new Supplier<List<String>>() {
      @Override public List<String> get() {
        return Lists.newArrayList();
      }
    });
  }
}
