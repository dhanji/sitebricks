package com.google.sitebricks.binding;

import com.google.inject.ImplementedBy;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(HttpSessionFlashCache.class)
public interface FlashCache {
  <T> T get(String key);

  <T> void put(String key, T t);
}
