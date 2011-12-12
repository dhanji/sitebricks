package com.google.sitebricks.binding;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.jcip.annotations.ThreadSafe;

import javax.servlet.http.HttpSession;

/**
 * Used to store binding (or forwarding) information between successive requests. This
 * cache is an alternative to the default {@linkplain HttpSessionFlashCache} in that it
 * explicitly sets each attribute on the session (which is required by GAE for appstore
 * and memcache replication).
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe
public final class GaeFlashCache implements FlashCache {
  private final Provider<HttpSession> session;

  @Inject
  GaeFlashCache(Provider<HttpSession> session) {
    this.session = session;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) session.get().getAttribute(key);
  }

  public <T> T remove(String key) {
    @SuppressWarnings("unchecked")
    T previous = (T) get(key);
    session.get().removeAttribute(key);

    return previous;
  }

  public <T> void put(String key, T t) {
    session.get().setAttribute(key, t);
  }
}
