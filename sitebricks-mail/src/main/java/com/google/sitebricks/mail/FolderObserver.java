package com.google.sitebricks.mail;

import java.util.List;

/**
 * Listens for IMAP folder events such as new mail arriving.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface FolderObserver {
  /**
   * Mail change notification in this folder. The client may now
   * check for MessageStatuses.
   *
   * @param added the set of numbers of newly added messages, or null if none
   * @param removed the set of numbers of just removed messages, or null if none
   */
  void changed(List<Integer> added, List<Integer> removed);
}
