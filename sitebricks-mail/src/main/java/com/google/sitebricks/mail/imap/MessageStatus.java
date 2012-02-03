package com.google.sitebricks.mail.imap;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Represents a single email message.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MessageStatus {

  public static final MessageStatus KNOWN_MESSAGE_MARKER = new MessageStatus();

  private int imapUid;
  private String messageUid;
  private Date receivedDate;
  private String subject;
  private String inReplyTo;

  private List<String> from;
  private List<String> sender;
  private List<String> to;
  private List<String> cc;
  private List<String> bcc;
  private List<String> replyTo;

  private EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
  private Date internalDate;

  private int size;
  private Long threadId;
  private Set<String> labels;
  private Long gmailMsgId;


  public int getImapUid() {
    return imapUid;
  }

  public void setImapUid(int imapUid) {
    this.imapUid = imapUid;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public List<String> getFrom() {
    return from;
  }

  public void setFrom(List<String> from) {
    this.from = from;
  }

  public List<String> getSender() {
    return sender;
  }

  public void setSender(List<String> sender) {
    this.sender = sender;
  }

  public List<String> getTo() {
    return to;
  }

  public void setTo(List<String> to) {
    this.to = to;
  }

  public List<String> getCc() {
    return cc;
  }

  public void setCc(List<String> cc) {
    this.cc = cc;
  }

  public List<String> getBcc() {
    return bcc;
  }

  public void setBcc(List<String> bcc) {
    this.bcc = bcc;
  }

  public List<String> getReplyTo() {
    return replyTo;
  }

  public void setReplyTo(List<String> replyTo) {
    this.replyTo = replyTo;
  }

  public String getMessageUid() {
    return messageUid;
  }

  public Date getReceivedDate() {
    return receivedDate;
  }

  public Date getInternalDate() {
    return internalDate;
  }

  public String getSubject() {
    return subject;
  }

  public EnumSet<Flag> getFlags() {
    return flags;
  }

  public Long getThreadId() {
    return threadId;
  }

  public void setMessageUid(String messageUid) {
    this.messageUid = messageUid;
  }

  public void setReceivedDate(Date receivedDate) {
    this.receivedDate = receivedDate;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setThreadId(Long threadId) {
    this.threadId = threadId;
  }

  public void setInReplyTo(String inReplyTo) {
    this.inReplyTo = inReplyTo;
  }

  public void setFlags(EnumSet<Flag> flags) {
    this.flags = flags;
  }

  public void setLabels(Set<String> labels) {
    this.labels = labels;
  }

  public Set<String> getLabels() {
    return labels;
  }

  public void setInternalDate(Date internalDate) {
    this.internalDate = internalDate;
  }

  public void setGmailMsgId(Long gmailMsgId) {
    this.gmailMsgId = gmailMsgId;
  }

  public Long getGmailMsgId() {
    return gmailMsgId;
  }

  private static final SimpleDateFormat ISO_C_DATE_SYDNEY =
      new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy") {{
    setTimeZone(TimeZone.getTimeZone("Australia/Sydney"));
  }};


  @Override public String toString() {
    return "MessageStatus{" +
        "imapUid=" + imapUid +
        ", messageUid='" + messageUid + '\'' +
        ", receivedDate=" + (receivedDate == null ? "null" : ISO_C_DATE_SYDNEY.format(receivedDate)) +
        ", subject='" + subject + '\'' +
        ", inReplyTo='" + inReplyTo + '\'' +
        ", from=" + from +
        ", sender=" + sender +
        ", to=" + to +
        ", cc=" + cc +
        ", bcc=" + bcc +
        ", replyTo=" + replyTo +
        ", flags=" + flags +
        ", internalDate=" + ISO_C_DATE_SYDNEY.format(internalDate) +
        ", size=" + size +

        ((threadId != null) ?
            (", threadId=" + threadId +
            ", labels=" + labels)
        : "") +
        ((gmailMsgId != null) ?
            (", gmailMsgId=" + gmailMsgId)
        : "") +
        '}';
  }
}
