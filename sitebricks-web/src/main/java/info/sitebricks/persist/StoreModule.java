package info.sitebricks.persist;

import com.db4o.ObjectContainer;
import com.google.inject.servlet.RequestScoped;
import com.google.sitebricks.AwareModule;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class StoreModule extends AwareModule {
  @Override
  protected void configureLifecycle() {
    observe(PersistAware.class);

    bind(ObjectContainer.class).toProvider(PersistAware.class).in(RequestScoped.class);
  }
}
