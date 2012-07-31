package com.google.sitebricks;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHandler;

import javax.servlet.*;
import java.io.IOException;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class ServletContainerIntegrationTest {

  // @NOTaTest
  public final void fireUp() throws Exception {

    final Server server = new Server(8085);
    final ServletHandler servletHandler = new ServletHandler();
    servletHandler.addFilterWithMapping(new FilterHolder(new Filter() {
      public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("*************************************************");
        final Set resourcePaths = filterConfig.getServletContext().getResourcePaths("/WEB-INF/classes");

        System.out.println(resourcePaths);

        System.out.println("*************************************************");
      }

      public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("*************************************************");
        System.out.println("Hello!");
        System.out.println("*************************************************");
      }

      public void destroy() {
      }
    }), "/*", Handler.REQUEST);

    server.addHandler(servletHandler);

    server.start();
    server.join();
  }
}
