package com.google.sitebricks.binding;

import com.google.common.collect.MapMaker;
import com.google.inject.servlet.SessionScoped;
import net.jcip.annotations.ThreadSafe;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 *         <p/>
 *         Used to store binding (or forwarding) information between successive requests.
 */
@SessionScoped
@ThreadSafe
class HttpSessionFlashCache implements FlashCache {
  private final Map<String, Object> cache = new MapMaker().makeMap();

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) cache.get(key);
  }

  public <T> void put(String key, T t) {
    cache.put(key, t);
  }
}
