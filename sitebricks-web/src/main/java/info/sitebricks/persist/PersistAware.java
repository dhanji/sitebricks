package info.sitebricks.persist;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectServer;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.Aware;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class PersistAware implements Aware, Provider<ObjectContainer> {
  private ObjectServer objectServer;

  @Override @SuppressWarnings("deprecation")
  public void startup() {
    objectServer = Db4o.openServer("sitebricks-web.dat", 65535 /* port ignored */);
  }

  @Override
  public void shutdown() {
    objectServer.close();
  }

  @Override
  public ObjectContainer get() {
    return objectServer.openClient();
  }
}
