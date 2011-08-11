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

  @SuppressWarnings("unchecked") // Ugly gunk needed to prevent generics from spewing everywhere
  public CommandCompletion(Command command, Long sequence, SettableFuture<?> valueFuture) {
    this.valueFuture = (SettableFuture<Object>) valueFuture;
    this.sequence = sequence;
    this.command = command;
  }

  public boolean complete(String message) {
    // Base case (empty/newline message).
    if (message.isEmpty()) {
      value.add(message);
      return false;
    }

    String[] pieces = message.split("[ ]+", 2);

    String content = (pieces.length > 1) ? pieces[1] : message;
    String status = content.toLowerCase();
    if (Command.isEndOfSequence(status)) {
      // Ensure sequencing was correct.
      if (!Long.valueOf(pieces[0]).equals(sequence)) {
        log.error("Sequencing incorrect, expected {} but was {} ", sequence, pieces[0]);
      }

      // Once we see the OK message, we should process the data and return.
      value.add(content);
      valueFuture.set(command.extract(value));
      return true;
    }

    value.add(content);

    return false;
  }
}
