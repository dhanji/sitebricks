package info.sitebricks.persist;

import com.db4o.ObjectContainer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class PersistFilter implements Filter {
  private final Provider<ObjectContainer> container;

  @Inject
  public PersistFilter(Provider<ObjectContainer> container) {
    this.container = container;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {
    try {
      container.get();
      filterChain.doFilter(servletRequest, servletResponse);
    } finally {
      container.get().commit();
    }
  }

  @Override
  public void destroy() {
  }
}
