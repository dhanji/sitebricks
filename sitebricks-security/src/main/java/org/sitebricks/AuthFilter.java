package org.sitebricks;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class AuthFilter implements Filter {
  private final SessionControl sessionControl;
  private final UserSessionScope sessionScope;

  @Inject
  public AuthFilter(SessionControl sessionControl, UserSessionScope sessionScope) {
    this.sessionControl = sessionControl;
    this.sessionScope = sessionScope;
  }

  public void init(FilterConfig filterConfig) throws ServletException {
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;

    UserSession session = null;
    // Resurrect the session if possible.
    Cookie cookie = Cookies.readSessionCookie(request);
    if (null != cookie) {
      session = sessionControl.getByCookie(cookie.getValue());
      // Attempt to revive session from persistent store if it's not in memory (helps
      // us survive across server restarts seamlessly).
      if (session == null)
        if (tryResume(cookie)) {
          session = sessionControl.getByCookie(cookie.getValue());

        } else {
          Cookies.expireSessionCookie(request, (HttpServletResponse) servletResponse);
        }
    }

    // Allow requests through, except those to /r/*
    String requestUri = request.getRequestURI();
    if (requestUri == null || "/".equals(requestUri))
      ((HttpServletResponse) servletResponse).sendRedirect("/b/default");

    if (isPublicRequest(requestUri)) {
      try {
        // Create the "not-logged-in" session, if need be
        if (session == null)
          session = new UserSession();
        sessionScope.enter(session);
        filterChain.doFilter(servletRequest, servletResponse);
      } finally {
        sessionScope.exit();
      }
    } else {

      // Allow all requests through if a user is logged in.
      if (session != null) {
        try {

          sessionScope.enter(session);
          filterChain.doFilter(servletRequest, servletResponse);
        } finally {
          sessionScope.exit();
        }
      } else
        ((HttpServletResponse) servletResponse).sendError(HttpServletResponse.SC_FORBIDDEN,
            "You must be logged in to interact with the pasteboard");
    }
  }

  private static boolean isPublicRequest(String requestUri) {
    return !requestUri.startsWith("/r/");
  }

  boolean tryResume(Cookie cookie) {
    return sessionControl.resume(cookie.getValue());
  }

  public void destroy() {
  }
}