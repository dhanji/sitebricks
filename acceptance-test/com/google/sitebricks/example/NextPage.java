package com.google.sitebricks.example;

/**
 * The target page that receives the state.
 *
 * @author dhanji@google.com (Dhanji R. Prasanna)
 */
public class NextPage {
  private String persistedValue;

  public NextPage(String persistedValue) {
    this.persistedValue = persistedValue;
  }

  public String getPersistedValue() {
    return persistedValue;
  }
}