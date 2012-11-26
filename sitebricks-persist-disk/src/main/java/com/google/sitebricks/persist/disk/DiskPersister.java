package com.google.sitebricks.persist.disk;

import com.google.sitebricks.persist.EntityStore;
import com.google.sitebricks.persist.Persister;

import javax.inject.Inject;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class DiskPersister extends Persister {
  private final IndexSet indexSet;

  @Inject
  private DiskEntityStore entityStore;

  public DiskPersister(String directory) {
    this.indexSet = new IndexSet(directory);
  }

  @Override
  public void start() {
    indexSet.startup();
    entityStore.init(indexSet);
  }

  @Override
  public void shutdown() {
    indexSet.shutdown();
  }

  @Override
  protected EntityStore beginWork() {
    return entityStore;
  }

  @Override
  protected void endWork(EntityStore store, boolean commit) {
    entityStore.complete(commit);
  }

  @Override
  protected EntityStore.EntityTransaction beginTransaction() {
    // Locking here ensures no other thread can interleave and dirty the underlying writer
    // while this transaction is in progress.
    entityStore.lock();
    return new EntityStore.EntityTransaction() {
      @Override
      public void commit() {
        entityStore.complete(true);
      }

      @Override
      public void rollback() {
        entityStore.complete(false);
      }
    };
  }
}
