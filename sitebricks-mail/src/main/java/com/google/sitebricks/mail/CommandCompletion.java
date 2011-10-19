package com.google.sitebricks.mail;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import com.google.sitebricks.mail.imap.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A generic command completion listener that aggregates incoming messages
 * until it forms a complete response to an issued command.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class CommandCompletion {
  private static final Logger log = LoggerFactory.getLogger(CommandCompletion.class);
  private final SettableFuture<Object> valueFuture;
  private final List<String> value = Lists.newArrayList();
  private final Long sequence;
  private final Command command;
  private final String commandString;

  @SuppressWarnings("unchecked") // Ugly gunk needed to prevent generics from spewing everywhere
  public CommandCompletion(Command command,
                           Long sequence,
                           SettableFuture<?> valueFuture,
                           String commandString) {
    this.commandString = commandString;
    this.valueFuture = (SettableFuture<Object>) valueFuture;
    this.sequence = sequence;
    this.command = command;
  }

  public void error(String message, Exception e) {
    StringBuilder builder = new StringBuilder();
    for (String piece : value) {
      builder.append(piece).append('\n');
    }
    log.error("Exception while processing response:\n Command: {} (seq: {})\n\n--message follows--" +
        "\n{}\n--message end--\n--context follows--\n{}\n--context end--\n\n",
        new Object[] { commandString, sequence, message, builder.toString(), e });

    // TODO Send this back to the client as an exception so it can be handled correctly.
//    valueFuture.setException(new MailHandlingException(value, message, e));
  }

  public boolean complete(String message) {
    // Base case (empty/newline message).
    if (message.isEmpty()) {
      value.add(message);
      return false;
    }

    if (Command.isEndOfSequence(sequence, message.toLowerCase())) {
      // Once we see the OK message, we should process the data and return.
      value.add(message);
      valueFuture.set(command.extract(value));
      return true;
    }

    value.add(message);

    return false;
  }
}
