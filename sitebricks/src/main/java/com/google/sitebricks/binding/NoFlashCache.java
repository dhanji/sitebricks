package com.google.sitebricks.binding;

import com.google.inject.Singleton;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class NoFlashCache implements FlashCache {
  @Override
  public <T> T get(String key) {
    return null;
  }

  @Override
  public <T> T remove(String key) {
    return null;
  }

  @Override
  public <T> void put(String key, T t) {
  }
}
