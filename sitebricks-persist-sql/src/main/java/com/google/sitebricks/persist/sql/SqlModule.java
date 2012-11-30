package com.google.sitebricks.persist.sql;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.sitebricks.persist.AbstractPersistenceModule;
import com.google.sitebricks.persist.Persister;
import com.jolbox.bonecp.BoneCPConfig;

import java.lang.annotation.Annotation;

/**
 * A simple disk-based object store. Will use any file system directory
 * specified (must be currently mounted).
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SqlModule extends AbstractPersistenceModule {
  private final BoneCPConfig config;

  public SqlModule(BoneCPConfig config) {
    this(null, config);
  }

  public SqlModule(Class<? extends Annotation> selector, BoneCPConfig config) {
    super(selector);
    this.config = config;
  }

  @Override
  protected void internalConfigure() {
    exposeEntityStoreDelegate(Sql.class);
  }

  @Provides @Singleton
  Persister providePersister() {
    SqlPersister persister = new SqlPersister(config);
//    requestInjection(persister);
    return persister;
  }
}
