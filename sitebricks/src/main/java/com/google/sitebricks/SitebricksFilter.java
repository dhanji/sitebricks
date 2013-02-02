package com.google.sitebricks;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jcip.annotations.Immutable;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.ReplyBasedHeadlessRenderer;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.routing.RoutingDispatcher;
import com.google.sitebricks.routing.RoutingDispatcher.Events;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable
@Singleton
class SitebricksFilter implements Filter {
  private final RoutingDispatcher dispatcher;
  private final Provider<Bootstrapper> bootstrapper;
  private final Provider<Shutdowner> teardowner;

  private final Provider<Request> requestProvider;
  private final ReplyBasedHeadlessRenderer headlessRenderer;

  @Inject
  public SitebricksFilter(RoutingDispatcher dispatcher, Provider<Bootstrapper> bootstrapper,
                          Provider<Shutdowner> teardowner, Provider<Request> requestProvider,
                          ReplyBasedHeadlessRenderer headlessRenderer) {
    this.dispatcher = dispatcher;
    this.bootstrapper = bootstrapper;
    this.teardowner = teardowner;
    this.requestProvider = requestProvider;
    this.headlessRenderer = headlessRenderer;
  }

  public void init(FilterConfig filterConfig) throws ServletException {
    bootstrapper.get().start();
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    //dispatch
    Object respondObject = dispatcher.dispatch(this.requestProvider.get(), Events.DURING);

    //was there any matching page? (if it was a headless response, we don't need to do anything).
    // Also we do not do anything if the page elected to do nothing.
    if (null != respondObject && null == request.getAttribute(Reply.NO_REPLY_ATTR)) {

      // Only use the string rendering pipeline if this is not a headless request.
      if (respondObject instanceof Respond) {
        Respond respond = (Respond) respondObject;
      
        //do we need to redirect or was this a successful render?
        final String redirect = respond.getRedirect();
        if (null != redirect) {
            // We need to encode in a way browser is fine with special characters when reloading the page.
            String redirectEncoded = URLEncoder.encode(redirect, "UTF-8").replaceAll("%2F", "/");
            response.sendRedirect(redirectEncoded);
            } else { //successful render

          // by checking if a content type was set, we allow users to override content-type
          //  on an arbitrary basis
          if (null == response.getContentType()) {
            response.setContentType(respond.getContentType());
          }

          response.getWriter().write(respond.toString());
        }
      } else { // It must be a headless Reply. Render the headless response.
        headlessRenderer.render(response, respondObject);
      }
    } else {
      //continue down filter-chain
      filterChain.doFilter(request, response);
    }
  }

  public void destroy() {
    teardowner.get().shutdown();
  }
}
