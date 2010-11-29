package com.google.sitebricks.binding;

import com.google.common.collect.MapMaker;
import com.google.inject.servlet.SessionScoped;
import net.jcip.annotations.ThreadSafe;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * Used to store binding (or forwarding) information between successive requests.
 * 
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe @SessionScoped
public class HttpSessionFlashCache implements FlashCache, Serializable {
  private final ConcurrentMap<String, Object> cache = new MapMaker().makeMap();

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) cache.get(key);
  }

  @SuppressWarnings("unchecked")
  public <T> T remove(String key) {
    return (T) cache.remove(key);
  }

  public <T> void put(String key, T t) {
    cache.put(key, t);
  }
}
