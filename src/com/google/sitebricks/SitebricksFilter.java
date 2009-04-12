package com.google.sitebricks;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.routing.RoutingDispatcher;
import net.jcip.annotations.Immutable;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Immutable @Singleton
class SitebricksFilter implements Filter {
    private final RoutingDispatcher dispatcher;
    private final Provider<ContextInitializer> initializer;

    @Inject
    public SitebricksFilter(RoutingDispatcher dispatcher, Provider<ContextInitializer> initializer) {
        this.dispatcher = dispatcher;
        this.initializer = initializer;
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        initializer.get().init(filterConfig.getServletContext());
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //dispatch
        final Respond respond = dispatcher.dispatch(request);

        //was there any matching page?
        if (null != respond) {

            //do we need to redirect or was this a successful render?
            final String redirect = respond.getRedirect();
            if (null != redirect)
                response.sendRedirect(redirect);

            else {                //successful render

                //by checking if a content type was set, we allow users to override content-type on an arbitrary basis
                if (null == response.getContentType())
                    response.setContentType(respond.getContentType());

                response.getWriter().write(respond.toString());
            }
        } else {
            //continue down filter-chain
            filterChain.doFilter(request, response);
        }
    }

    public void destroy() {
    }
}
