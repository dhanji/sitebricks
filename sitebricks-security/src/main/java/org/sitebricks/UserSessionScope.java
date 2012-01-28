package org.sitebricks;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * Special simulated proxy session scope for use with UserSession objects ONLY!
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class UserSessionScope implements Scope {
  private static final ThreadLocal<UserSession> sessionThreadLocal = new ThreadLocal<UserSession>();

  @Override public <T> Provider<T> scope(final Key<T> tKey, final Provider<T> tProvider) {
    return new Provider<T>() {
      @Override public T get() {
        // If we have it in websocket scope, use that.
        UserSession session = sessionThreadLocal.get();
        if (session != null)
          return (T) session;

        throw new OutOfScopeException("Session is not active!!");
      }
    };
  }

  public void enter(UserSession session) {
    Preconditions.checkState(sessionThreadLocal.get() == null,
        "Already a session in progress. FATAL--Threads crossing!!!");
    sessionThreadLocal.set(session);
  }

  public void exit() {
    Preconditions.checkState(sessionThreadLocal.get() != null,
        "Someone already evicted this session");
    sessionThreadLocal.remove();
  }
}