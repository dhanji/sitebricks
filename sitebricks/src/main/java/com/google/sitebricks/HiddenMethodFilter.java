
package com.google.sitebricks;

import com.google.inject.Singleton;
import com.google.sitebricks.rendering.Strings;
import net.jcip.annotations.Immutable;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import java.io.ByteArrayInputStream;
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
class HiddenMethodFilter implements Filter {

  private static final String FILTER_DONE_SUFFIX = "__done";
  private static final String HIDDEN_FIELD_NAME = "hiddenFieldName";

  /**
   * Name of the hidden field. Shared across the entire App. Is guarded by
   * init so no danger of visibility issues.
   */
  static String hiddenFieldName = "__sitebricks__action";
  private String filterDoneAttributeName;

  public void init(FilterConfig filterConfig) throws ServletException {
    String param = filterConfig.getInitParameter(HIDDEN_FIELD_NAME);
    if (param != null) {
      hiddenFieldName = param;
    }

    // Request attribute name to signal that filtering was already done in a request
    filterDoneAttributeName = hiddenFieldName + FILTER_DONE_SUFFIX;
  }


  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {

    HttpServletRequest httpRequest = (HttpServletRequest) request;

    // check if filtering was already done on this request
    if (httpRequest.getAttribute(filterDoneAttributeName) != null) {
      // Filtering done, forward to another filter in chain
      filterChain.doFilter(httpRequest, response);

    } else {
      httpRequest.setAttribute(filterDoneAttributeName, Boolean.TRUE);

      try {
        ServletInputStream inputStream = httpRequest.getInputStream();
        byte[] reqBytes = new byte[httpRequest.getContentLength()];
        inputStream.read(reqBytes);

        // Making the input stream available again because we have
        // already read bytes
        HttpServletRequestWrapper wrappedRequest = getWrappedRequest(httpRequest, reqBytes);
        String methodName = wrappedRequest.getParameter(this.hiddenFieldName);

        if ("POST".equalsIgnoreCase(httpRequest.getMethod()) && !Strings.empty(methodName)) {
          String methodNameUppercase = methodName.toUpperCase(Locale.ENGLISH);
          HttpServletRequest wrapper = new HttpMethodRequestWrapper(methodNameUppercase, httpRequest);
          filterChain.doFilter(wrapper, response);
        } else {
          // Making the input stream available again fix for issue 45
          wrappedRequest = getWrappedRequest(httpRequest, reqBytes);
          //Filtering done, forward to another filter in chain
          filterChain.doFilter(wrappedRequest, response);
        }
      } finally {
        // Remove the filterDone attribute for this request.
        request.removeAttribute(filterDoneAttributeName);
      }
    }
  }
  
  private HttpServletRequestWrapper getWrappedRequest(HttpServletRequest httpRequest, final byte[] reqBytes)
       throws IOException {

    final ByteArrayInputStream byteInput = new ByteArrayInputStream(reqBytes);
    return new HttpServletRequestWrapper(httpRequest) {
     
      @Override
      public ServletInputStream getInputStream() throws IOException {
        ServletInputStream sis = new ServletInputStream() {

          @Override
          public int read() throws IOException {
            return byteInput.read();
          }
        };
        return sis;
      }
    };
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
