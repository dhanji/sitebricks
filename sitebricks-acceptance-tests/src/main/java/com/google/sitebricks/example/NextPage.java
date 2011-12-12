package com.google.sitebricks.example;

import com.google.sitebricks.At;

/**
 * The target page that receives the state.
 *
 * @author dhanji@google.com (Dhanji R. Prasanna)
 */
@At("/nextpage")
public class NextPage {
  private String persistedValue;

  public NextPage(String persistedValue) {
    this.persistedValue = persistedValue;
  }

  public String getPersistedValue() {
    return persistedValue;
  }
}