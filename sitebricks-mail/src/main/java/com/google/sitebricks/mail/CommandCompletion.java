package com.google.sitebricks.mail;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ValueFuture;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class CommandCompletion {
  private final ValueFuture<List<String>> valueFuture;

  public CommandCompletion(ValueFuture<List<String>> valueFuture) {
    this.valueFuture = valueFuture;
  }

  public void complete(String message) {
    System.out.println("Completing! " + message);
    valueFuture.set(ImmutableList.<String>of());
  }
}
