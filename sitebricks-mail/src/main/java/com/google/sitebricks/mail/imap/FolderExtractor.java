package com.google.sitebricks.mail.imap;

import com.google.common.base.Preconditions;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class FolderExtractor implements Extractor<Folder> {

  private static final String SELECTED = "selected";

  @Override
  public Folder extract(List<String> messages) {
    String folderName = null;
    int count = 0;
    for (String message : messages) {
      String[] pieces = message.split("[ ]+", 3);
      if (pieces.length > 1 && "EXISTS".equalsIgnoreCase(pieces[1])) {
        count = Integer.valueOf(pieces[0]);
      } else if (message.contains(SELECTED)) {
        // Extract folder name as given by the server.
        int left = message.indexOf(pieces[1]) + pieces[1].length();
        folderName = message.substring(left, message.indexOf(SELECTED)).trim();
      }
    }

    Preconditions.checkState(null != folderName, "Error in IMAP protocol, " +
        "could not detect folder name");

    Folder folder = new Folder(folderName);
    folder.setCount(count);
    return folder;
  }
}
