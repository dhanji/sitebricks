package org.sitebricks;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Cookies {
  static final String _COOKIE_NAME = "__APP_USER_SESSION_ID";
  static final int TWO_WEEKS = 14 * 24 * 60 * 60;
  static final int SIX_MONTHS = 6 * 30 * 24 * 60 * 60;
  private static final int EXPIRE_NOW = 0;

  public static Cookie readSessionCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    for (Cookie cookie : cookies) {
      if (_COOKIE_NAME.equals(cookie.getName())) {
        return cookie;
      }
    }

    // Not found.
    return null;
  }

  /**
   * Creates a new session cookie.
   */
  public static String newSessionCookie(String user) {
    return "cke:" + UUID.randomUUID();
  }

  public static void expireSessionCookie(HttpServletRequest request, HttpServletResponse response) {
    Cookie cookie = readSessionCookie(request);
    if (null != cookie)
      writeSessionCookie(response, cookie.getValue(), EXPIRE_NOW);
  }

  public static void setSessionCookie(HttpServletResponse response, String value) {
    writeSessionCookie(response, value, SIX_MONTHS);
  }

  private static void writeSessionCookie(HttpServletResponse response, String value, int maxAge) {
    Cookie cookie = new Cookie(_COOKIE_NAME, value);
    cookie.setSecure(true);
    cookie.setMaxAge(maxAge); // in seconds.
    cookie.setPath("/");

    response.addCookie(cookie);
  }
}
