package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.sitebricks.mail.imap.Folder;
import com.google.sitebricks.mail.imap.FolderStatus;
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

  ListenableFuture<Folder> open(String folder);

  ListenableFuture<List<MessageStatus>> list(Folder folder, int start, int end);

  void watch(Folder folder, FolderObserver observer);

  void unwatch();
}
