package org.sitebricks;

import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A service for managing active user browsing sessions.
 * 
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class SessionControl {
  private static final Logger log = LoggerFactory.getLogger(SessionControl.class);
  private final UserStore userStore;

  // Quick Map of cookie id -> session.
  private final ConcurrentMap<String, UserSession> cookieSessions = new MapMaker().makeMap();

  @Inject
  public SessionControl(UserStore userStore) {
    this.userStore = userStore;
  }

  public UserSession getByCookie(String cookie) {
    return cookieSessions.get(cookie);
  }

  public String start(User user, DBObject rawUser) {
    UserSession userSession = new UserSession();
    String cookie;
    if (user != null) {
      // Save this new cookie to the database.
      cookie = Cookies.newSessionCookie(user.getName());
      user.getCookies().add(cookie);
      userStore.store(user);
      userSession.associate(user);
    } else {
      // Save this new cookie to the database.
      String name = rawUser.get("name").toString();
      cookie = Cookies.newSessionCookie(name);

      BasicDBList cookies = (BasicDBList) rawUser.get("cookies");
      cookies.add(cookie);
      userStore.replaceField(User.class, new BasicDBObject("name", name), ImmutableMap.<String, Object> of("cookies", cookies));
      userSession.associate(rawUser);
    }

    cookieSessions.put(cookie, userSession);
    return cookie;
  }

  public boolean resume(String cookie) {
    // First see if we already have this session in memory.
    UserSession session = cookieSessions.get(cookie);

    if (null == session) {
      DBObject found = userStore.findRaw(User.class, new BasicDBObject("cookies", cookie));
      // We don't know about this session--looks like a bogus cookie.
      if (found == null) {
        log.warn("Unknown cookie received: '{}'. Denying... ", cookie);
        return false;
      }
      session = new UserSession();
      session.associate(found);

      if (null != cookieSessions.put(cookie, session))
        throw new IllegalStateException("Session was already stashed by a parallel thread");
      return true;
    }

    // This appears to be a pointless reconnect request. Just return true.
    return true;
  }

  public void stop(String cookie) {
    UserSession session = cookieSessions.remove(cookie);
    BasicDBObject query = new BasicDBObject("name", session.getCurrentUser().getName());
    DBObject user = userStore.findRaw(User.class, query);

    BasicDBList cookies = (BasicDBList) user.get("cookies");
    cookies.remove(cookie);
    userStore.replaceField(User.class, new BasicDBObject(query), ImmutableMap.<String, Object> of("cookies", cookies));
  }
}