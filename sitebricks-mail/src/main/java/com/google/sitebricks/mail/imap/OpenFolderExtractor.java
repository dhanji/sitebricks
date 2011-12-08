package com.google.sitebricks.mail.imap;

import com.google.common.base.Preconditions;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class OpenFolderExtractor implements Extractor<Folder> {

  private static final String SELECTED = "selected";

  @Override
  public Folder extract(List<String> messages) {
    String folderName = null;
    int count = 0;
    for (String message : messages) {
      String[] pieces = message.split("[ ]+", 4);
      if (pieces.length > 1 && "EXISTS".equalsIgnoreCase(pieces[2])) {
        count = Integer.valueOf(pieces[1]);
      } else if (message.contains(SELECTED)) {
        // Extract folder name as given by the server.
        int left = message.indexOf(pieces[2]) + pieces[2].length();
        folderName = message.substring(left, message.indexOf(SELECTED)).trim();
      }
    }

    Preconditions.checkState(null != folderName, "Error in IMAP protocol, " +
        "could not detect folder name");

    return new Folder(folderName, count);
  }
}
