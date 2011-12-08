package com.google.sitebricks.mail.imap;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class FolderStatusExtractor implements Extractor<FolderStatus> {
  private static final Pattern PARENS = Pattern.compile("([(].*[)])");

  @Override
  public FolderStatus extract(List<String> messages) {
    FolderStatus status = new FolderStatus();

    // There should generally only be 1.
    for (String message : messages) {
      Matcher matcher = PARENS.matcher(message);
      if (matcher.find()) {
        String group = matcher.group(1);

        // Strip parens.
        group = group.substring(1, group.length() - 1);
        String[] pieces = group.split("[ ]+");
        for (int i = 0; i < pieces.length; i += 2) {
          String piece = pieces[i].toUpperCase();
          if ("MESSAGES".equals(piece)) {
            status.setMessages(Integer.valueOf(pieces[i + 1]));
          } else if ("UNSEEN".equals(piece)) {
            status.setUnseen(Integer.valueOf(pieces[i + 1]));
          } else if ("RECENT".equals(piece)) {
            status.setRecent(Integer.valueOf(pieces[i + 1]));
          } else if ("UIDNEXT".equals(piece)) {
            status.setNextUid(Integer.valueOf(pieces[i + 1]));
          } else if ("UIDVALIDITY".equals(piece)) {
            status.setUidValidity(Integer.valueOf(pieces[i + 1]));
          }
        }
      }
    }
    return status;
  }
}
