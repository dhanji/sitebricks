package com.google.sitebricks.mail.imap;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public enum Command {
  LIST_FOLDERS("list"),
  FETCH_BODY("fetch"),
  FETCH_BODY_UID("uid fetch"),
  FOLDER_STATUS("status"),
  FOLDER_OPEN("select"),
  FOLDER_EXAMINE("examine"),
  FETCH_HEADERS("fetch"),
  IDLE("idle"),
  STORE_FLAGS("uid store"),
  STORE_LABELS("uid store");
  private static final Pattern OK_SUCCESS = Pattern.compile("\\d+ ok (.* )?\\(?success\\)?",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern NO_FAILURE = Pattern.compile("\\d+ no .*",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern BAD_FAILURE = Pattern.compile("\\d+ bad .*",
      Pattern.CASE_INSENSITIVE);

  private final String commandString;
  private Command(String commandString) {
    this.commandString = commandString;
  }

  private static final Pattern IMAP_COMMAND_SUCCESS = Pattern.compile("\\d+ ok success",
      Pattern.CASE_INSENSITIVE);
  private static final Map<Command, Extractor<?>> dataExtractors;

  /**
   * Expects message to be lower case.
   */
  public static boolean isEndOfSequence(Long sequence, String message) throws ExtractionException {
    final String prefix = Long.toString(sequence) + " ";

    if (message.length() < (prefix.length()) || !prefix.equals(message.substring(0, prefix.length())))
      return false;

    if (OK_SUCCESS.matcher(message).matches())
      return true;

    if (NO_FAILURE.matcher(message).matches() ||
           BAD_FAILURE.matcher(message).matches()) {
      throw new ExtractionException(message);
    }
    return false;
  }

  public static boolean isEndOfSequence(String message) throws ExtractionException {
    if (IMAP_COMMAND_SUCCESS.matcher(message).matches())
      return true;

    if (NO_FAILURE.matcher(message).matches() ||
           (BAD_FAILURE.matcher(message).matches())) {
      throw new ExtractionException(message);
    }
    return false;
  }

  static {
    dataExtractors = Maps.newHashMap();

    dataExtractors.put(LIST_FOLDERS, new ListFoldersExtractor());
    dataExtractors.put(FOLDER_STATUS, new FolderStatusExtractor());
    dataExtractors.put(FOLDER_OPEN, new OpenFolderExtractor());
    dataExtractors.put(FOLDER_EXAMINE, new OpenFolderExtractor());
    dataExtractors.put(FETCH_HEADERS, new MessageStatusExtractor());
    dataExtractors.put(FETCH_BODY, new MessageBodyExtractor());
    dataExtractors.put(FETCH_BODY_UID, new SingleMessageBodyExtractor());
    dataExtractors.put(STORE_FLAGS, new StoreFlagsResponseExtractor());
    dataExtractors.put(STORE_LABELS, new StoreLabelsResponseExtractor());
  }

  @SuppressWarnings("unchecked") // Heterogenous collections are a pita in Java.
  public <D> D extract(List<String> message) throws ExtractionException {
    return (D) dataExtractors.get(this).extract(message);
  }

  @Override
  public String toString() {
    return commandString;
  }
}
