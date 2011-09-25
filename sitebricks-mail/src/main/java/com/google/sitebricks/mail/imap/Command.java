package com.google.sitebricks.mail.imap;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public enum Command {
  LIST_FOLDERS("list"),
  FETCH_BODY("fetch"),
  FOLDER_STATUS("status"),
  FOLDER_OPEN("select"),
  FOLDER_EXAMINE("examine"),
  FETCH_HEADERS("fetch"),
  IDLE("idle"); // IMAP4 IDLE command: http://www.ietf.org/rfc/rfc2177.txt

  private final String commandString;
  private Command(String commandString) {
    this.commandString = commandString;
  }

  private static final Map<Command, Extractor<?>> dataExtractors;

  public static boolean isEndOfSequence(String status) {
    return status.startsWith("ok") && status.contains("success");
  }

  static {
    dataExtractors = Maps.newHashMap();

    dataExtractors.put(LIST_FOLDERS, new ListFoldersExtractor());
    dataExtractors.put(FOLDER_STATUS, new FolderStatusExtractor());
    dataExtractors.put(FOLDER_OPEN, new OpenFolderExtractor());
    dataExtractors.put(FOLDER_EXAMINE, new OpenFolderExtractor());
    dataExtractors.put(FETCH_HEADERS, new MessageStatusExtractor());
    dataExtractors.put(FETCH_BODY, new MessageBodyExtractor());
  }

  @SuppressWarnings("unchecked") // Heterogenous collections are a pita in Java.
  public <D> D extract(List<String> message) {
    return (D) dataExtractors.get(this).extract(message);
  }

  @Override
  public String toString() {
    return commandString;
  }
}
