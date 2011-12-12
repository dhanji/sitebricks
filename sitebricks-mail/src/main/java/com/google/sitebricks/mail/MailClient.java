package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.sitebricks.mail.imap.Folder;
import com.google.sitebricks.mail.imap.FolderStatus;
import com.google.sitebricks.mail.imap.Message;
import com.google.sitebricks.mail.imap.MessageStatus;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface MailClient {
  /**
   * Connects to the IMAP server logs in with the given credentials.
   */
  void connect();

  /**
   * Logs out of the current IMAP session and releases all resources, including
   * executor services.
   */
  void disconnect();

  List<String> capabilities();

  ListenableFuture<List<String>> listFolders();

  ListenableFuture<FolderStatus> statusOf(String folder);

  /**
   * Opens a logical 'session' to the given folder name. This method must be called
   * prior to using many of the in-folder methods on this API.
   */
  ListenableFuture<Folder> open(String folder);

  /**
   * Returns a list of message headers in the given folder, between {@code start}
   * and {@code end}. The start is a 1-based index into the current session's "view"
   * of an IMAP folder. The message numbers are guaranteed not to change UNLESS
   * new mail is added or removed during a session (listen for this using the
   * {@link #watch(Folder, FolderObserver)} method.
   * <p>
   * The returned list is in ascending order (start : end) determined by the IMAP
   * server (the protocol requires chronological descending order--recent : old).
   * <p>
   * The messages returned in the list are lightweight {@link MessageStatus} objects
   * which only contain cursory information and some metadata about a message,
   * including the subject. This is useful to quickly obtain a list of messages
   * whose bodies can be fetched later with the more comprehensive
   * {@link #fetch(Folder, int, int)} method.
   * <p>
   * <b>NOTE: you must call {@link #open(String)} first.</b>
   */
  ListenableFuture<List<MessageStatus>> list(Folder folder, int start, int end);

  /**
   * Similar to {@link #list(Folder, int, int)} but fetches the entire message
   * instead of merely a header. Runs a bit slower as a result.
   * <p>
   * This returns the complete details of an email message. Prefer {@link #list(Folder, int, int)}
   * for fetching just subjects/status info as this method can be slower
   * for messages with large bodies.
   * <p>
   * <b>NOTE: you must call {@link #open(String)} first.</b>
   */
  public ListenableFuture<List<Message>> fetch(Folder folder, int start, int end);

  /**
   *
   * <p>
   * <b>NOTE: you must call {@link #open(String)} first.</b>
   */
  void watch(Folder folder, FolderObserver observer);

  /**
   * Stops watching a folder if one was currently being watched (otherwise
   * a noop). Events from 'IDLEing' will immediately stop when this method
   * returns and the registered {@link FolderObserver} will be forgotten.
   * <p>
   * Note the subtle point that this method (though it doesn't block) will
   * immediately stop firing events to its FolderObserver. This happens
   * even before IDLEing ceases on the server.
   */
  void unwatch();
}
