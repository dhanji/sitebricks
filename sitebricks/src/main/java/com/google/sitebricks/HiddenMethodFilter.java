
package com.google.sitebricks;

import com.google.inject.Singleton;
import net.jcip.annotations.Immutable;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Enables browsers making a simulated PUT and DELETE requests. Currently browsers support making
 * GET and POST requests. This {@link javax.servlet.Filter} checks if a hidden field is set and
 * renames HTTP method, retrieved via {@link javax.servlet.http.HttpServletRequest#getMethod()} to a method
 * set in the hidden field
 *
 * @author Peter Knego
 */
@Immutable
@Singleton
public final class HiddenMethodFilter implements Filter {

  private static final String FILTER_DONE_SUFFIX = "__done";
  private static final String HIDDEN_FIELD_NAME = "hiddenFieldName";

  /**
   * Name of the hidden field.
   */
  private String hiddenFieldName = "__sitebricks__action";


  public void setHiddenFieldName(String hiddenFieldName) {
    this.hiddenFieldName = hiddenFieldName;
  }


  public void init(FilterConfig filterConfig) throws ServletException {
    String param = filterConfig.getInitParameter(HIDDEN_FIELD_NAME);
    if (param != null) {
      hiddenFieldName = param;
    }
  }


  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
      throw new ServletException("HiddenMethodFilter supports just HTTP requests");
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;

    // create a new Request attribute name to signal that filtering was already done in this request
    String filterDoneAttributeName = hiddenFieldName + FILTER_DONE_SUFFIX;

    // check if filtering was already done on this request
    if (httpRequest.getAttribute(filterDoneAttributeName) != null) {
      // Filtering done, forward to another filter in chain
      filterChain.doFilter(httpRequest, response);

    } else {
      httpRequest.setAttribute(filterDoneAttributeName, Boolean.TRUE);

      try {
        String methodName = httpRequest.getParameter(this.hiddenFieldName);

        if ("POST".equals(httpRequest.getMethod()) && hasLength(methodName)) {
          String methodNameUppercase = methodName.toUpperCase(Locale.ENGLISH);
          HttpServletRequest wrapper = new HttpMethodRequestWrapper(methodNameUppercase, httpRequest);
          filterChain.doFilter(wrapper, response);
        } else {

          // Filtering done, forward to another filter in chain
          filterChain.doFilter(httpRequest, response);
        }
      } finally {
        // Remove the filterDone attribute for this request.
        request.removeAttribute(filterDoneAttributeName);
      }
    }
  }


  private boolean hasLength(String string) {
    return !(string == null || string.length() == 0);
  }


  public void destroy() {
  }


  private static class HttpMethodRequestWrapper extends HttpServletRequestWrapper {

    private final String method;

    public HttpMethodRequestWrapper(String method, HttpServletRequest request) {
      super(request);
      this.method = method;
    }

    
    @Override
    public String getMethod() {
      return this.method;
    }
  }

}
