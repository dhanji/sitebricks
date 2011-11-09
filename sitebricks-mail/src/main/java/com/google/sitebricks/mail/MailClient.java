package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.sitebricks.mail.imap.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * An IMAP mail client based on JBoss Netty and Event-driven IO.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface MailClient {
  /**
   * Connects to the IMAP server logs in with the given credentials. Waits until the
   * connection is established before returning. Returns true if the connection auth
   * was successful. If not, the login error message can be obtained by calling
   * {@link #lastError()}
   */
  boolean connect();

  /**
   * Identical to {@link #connect()}.
   *
   * @param listener A listener to be notified when this client is disconnected due
   *   to any IO error or closed normally. Can be null.
   */
  boolean connect(DisconnectListener listener);

  /**
   * Logs out of the current IMAP session and releases all resources, including
   * executor services.
   */
  void disconnect();

  /**
   * Returns true if the underlying channels are connected to the remote server and
   * open for business.
   */
  boolean isConnected();

  List<String> capabilities();

  ListenableFuture<List<String>> listFolders();

  ListenableFuture<FolderStatus> statusOf(String folder);

  /**
   * Opens a logical 'session' to the given folder name. This method must be called
   * prior to using many of the in-folder methods on this API. Opens folder in
   * READ-ONLY mode.
   * <p>
   * Equivalent of calling {@link #open(String, boolean)} with readWrite as false.
   */
  ListenableFuture<Folder> open(String folder);

  /**
   * Opens a logical 'session' to the given folder name. This method must be called
   * prior to using many of the in-folder methods on this API.
   * @param folder The canonical name of the folder on the IMAP server.
   * @param readWrite If true, will open the folder in READ-WRITE mode. Otherwise,
   *   in READ-ONLY mode.
   */
  ListenableFuture<Folder> open(String folder, boolean readWrite);

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
   * Adds flags to a range of messages.
   *
   * @return the new flags on the message, null on failure.
   * <b>NOTE: these can be different to those set due to concurrent updates by other clients.</b>
   * <b>NOTE: you must call {@link #connect()} first.</b>
   */
  ListenableFuture<Set<Flag>> addFlags(Folder folder, Set<Flag> flags, int imapUid);

  /**
   * Removes flags from a range of messages.
   *
   * @return the new flags on the message, null on failure.
   * <b>NOTE: these can be different to those set due to concurrent updates by other clients.</b>
   * <b>NOTE: you must call {@link #connect()} first.</b>
   */
  ListenableFuture<Set<Flag>> removeFlags(Folder folder, Set<Flag> flags, int imapUid);

  /**
   * Adds Gmail labels to a range of messages.
   *
   * @return the new labels on the message, null on failure.
   * <b>NOTE: these can be different to those set due to concurrent updates by other clients.</b>
   * <b>NOTE: you must call {@link #connect()} first.</b>
   */
  ListenableFuture<Set<String>> addGmailLabels(Folder folder, Set<String> labels, int imapUid);

  /**
   * Removes Gmail labels from a range of messages.
   *
   * @return the new labels on the message, null on failure.
   * <b>NOTE: these can be different to those set due to concurrent updates by other clients.</b>
   * <b>NOTE: you must call {@link #connect()} first.</b>
   */
  ListenableFuture<Set<String>> removeGmailLabels(Folder folder, Set<String> labels, int imapUid);

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
   * Watches a folder for changes. This is an implementation of the IMAP IDLE command and
   * is the preferred method for push notification.
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

  /**
   * Returns a string containing the last error message from the server or
   * null if no errors occurred recently.
   */
  String lastError();

  /**
   * Returns true if this client has successfully entered and is currently in IMAP IDLE.
   */
  boolean isIdling();

  /**
   * Allows you to dynamically update the value of access token and tokenSecret if using
   * XOAuth to access an imap server. This is useful if a user logs in from multiple locations
   * and you always want the last authorized token to be used when reconnecting existing mail
   * clients.
   *
   * @param accessToken A string containing the access token as retrieved from the oauth server.
   * @param tokenSecret A string contianing the token secret pair of the accessToken.
   */
  void updateOAuthAccessToken(String accessToken, String tokenSecret);

  static interface DisconnectListener {
    void disconnected();

    /**
     * Called when the server acknowledges IDLE.
     */
    void idled();

    /**
     * Called when the server acknowledges exit from IDLE.
     */
    void unidled();
  }
}
