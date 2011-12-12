package com.google.sitebricks.mail;

/**
 * Listens for IMAP folder events such as new mail arriving.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface FolderObserver {
  /**
   * New mail arrived in this folder. The client should now
   * check for new MessageStatuses.
   */
  void onMailAdded();

  /**
   * Existing mail was expunged from this folder. This could
   * happen via other clients or the activation of server-side
   * filters for example.
   */
  void onMailRemoved();
}
