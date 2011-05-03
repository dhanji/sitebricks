package com.google.sitebricks.mail.imap;

import java.util.Date;
import java.util.EnumSet;

/**
 * Represents a single email message.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageStatus {
  private final String messageUid;
  private final Date receivedDate;
  private final String subject;
  private final EnumSet<Flag> flags;

  private final String from;
  private final String sender;
  private final String replyTo;

  public MessageStatus(String messageUid,
                       Date receivedDate,
                       String subject,
                       EnumSet<Flag> flags,
                       String from, String sender, String replyTo) {
    this.messageUid = messageUid;
    this.receivedDate = receivedDate;
    this.subject = subject;
    this.flags = flags;
    this.from = from;
    this.sender = sender;
    this.replyTo = replyTo;
  }

  public String getMessageUid() {
    return messageUid;
  }

  public Date getReceivedDate() {
    return receivedDate;
  }

  public String getSubject() {
    return subject;
  }

  public EnumSet<Flag> getFlags() {
    return flags;
  }

  @Override
  public String toString() {
    return "MessageStatus{" +
        "messageUid='" + messageUid + '\'' +
        ", receivedDate=" + receivedDate +
        ", subject='" + subject + '\'' +
        ", flags=" + flags +
        ", from='" + from + '\'' +
        ", sender='" + sender + '\'' +
        ", replyTo='" + replyTo + '\'' +
        '}';
  }
}
