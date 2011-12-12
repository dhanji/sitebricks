package com.google.sitebricks.binding;

import com.google.inject.ImplementedBy;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(NoFlashCache.class)
public interface FlashCache {
  @Nullable
  <T> T get(String key);

  @Nullable
  <T> T remove(String key);

  <T> void put(String key, T t);
}
