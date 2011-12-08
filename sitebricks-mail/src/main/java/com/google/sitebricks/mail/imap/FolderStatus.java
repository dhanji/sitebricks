package com.google.sitebricks.mail.imap;

/**
 * Some metadata about an IMAP folder.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class FolderStatus {
  private int messages;
  private int unseen;
  private int recent;
  private int nextUid;
  private int uidValidity;

  void setMessages(int messages) {
    this.messages = messages;
  }

  void setUnseen(int unseen) {
    this.unseen = unseen;
  }

  void setRecent(int recent) {
    this.recent = recent;
  }

  void setNextUid(int nextUid) {
    this.nextUid = nextUid;
  }

  public int getMessages() {
    return messages;
  }

  public int getUnseen() {
    return unseen;
  }

  public int getRecent() {
    return recent;
  }

  public int getNextUid() {
    return nextUid;
  }

  public int getUidValidity() {
    return uidValidity;
  }

  public void setUidValidity(int uidValidity) {
    this.uidValidity = uidValidity;
  }
  
  @Override
  public String toString() {
    return "FolderStatus{" +
        "messages=" + messages +
        ", unseen=" + unseen +
        ", recent=" + recent +
        ", nextUid=" + nextUid +
        '}';
  }

}
