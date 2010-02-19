package com.google.sitebricks.binding;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import net.jcip.annotations.ThreadSafe;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * Used to store binding (or forwarding) information between successive requests.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ThreadSafe @Singleton
class CookieBasedFlashCache implements FlashCache, Serializable {
  private final ConcurrentMap<String, Object> cache = new MapMaker()
      .concurrencyLevel(64)
      .makeMap();

  /**
   * Name of the cookie we use to create flash-scoping. I.e. consecutive
   * request detection (without sessions).
   */
  private static final String FLASH_COOKIE = "X-SB-Flash";
  private final Provider<HttpServletRequest> request;
  private final Provider<HttpServletResponse> response;

  @Inject
  public CookieBasedFlashCache(Provider<HttpServletRequest> request,
                               Provider<HttpServletResponse> response) {
    this.request = request;
    this.response = response;
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    String cookieId = findCookie();
    if (cookieId == null) {
      return null;
    }

    // This is how the cookie is constructed in {@linkplain #put}
    key = cookieId + key;

    return (T) cache.get(key);
  }

  @SuppressWarnings("unchecked")
  public <T> T remove(String key) {
    String cookieId = findCookie();
    if (cookieId == null) {
      return null;
    }
    
    // This is how the cookie is constructed in {@linkplain #put}
    key = cookieId + key;
    return (T) cache.remove(key);
  }

  public <T> void put(String key, T t) {

    String cookieId = (String) request.get().getAttribute(FLASH_COOKIE);
    if (null == cookieId) {
      // seed a cookie for the next time this user comes back
      cookieId = UUID.randomUUID().toString();
      response.get().addCookie(new Cookie(FLASH_COOKIE, cookieId));

      // memo for this request... (we only need to set the cookie once per request)
      request.get().setAttribute(FLASH_COOKIE, cookieId);
    }

    // Compose a key from the cookied id + the store key
    // We use the cookied id first coz the first 5 chars of
    // String are used for generating a hash.
    key = cookieId + key;

    cache.put(key, t);
  }

  private String findCookie() {
    String cookieId = null;
    for (Cookie cookie : request.get().getCookies()) {
      if (FLASH_COOKIE.equals(cookie.getName())) {
        cookieId = cookie.getValue();
      }
    }

    return cookieId;
  }
}