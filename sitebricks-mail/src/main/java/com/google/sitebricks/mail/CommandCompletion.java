package com.google.sitebricks.mail;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ValueFuture;
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
  private final ValueFuture<Object> valueFuture;
  private final List<String> value = Lists.newArrayList();
  private final Long sequence;
  private final Command command;

  @SuppressWarnings("unchecked") // Ugly gunk needed to prevent generics from spewing everywhere
  public CommandCompletion(Command command, Long sequence, ValueFuture<?> valueFuture) {
    this.valueFuture = (ValueFuture<Object>) valueFuture;
    this.sequence = sequence;
    this.command = command;
  }

  public boolean complete(String message) {
    String[] pieces = message.split("[ ]+", 2);

    String status = pieces[1].toLowerCase();
    if (status.startsWith("ok") && status.contains("success")) {
      // Ensure sequencing was correct.
      if (!Long.valueOf(pieces[0]).equals(sequence)) {
        log.error("Sequencing incorrect, expected {} but was {} ", sequence, pieces[0]);
      }

      // Give it the final message and then process the data.      
      value.add(pieces[1]);
      valueFuture.set(command.extract(value));
      return true;
    }

    value.add(pieces[1]);

    return false;
  }
}
