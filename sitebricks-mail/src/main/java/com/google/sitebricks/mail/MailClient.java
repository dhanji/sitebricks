package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;

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
}
