package com.google.sitebricks.persist.disk;

import com.google.sitebricks.persist.AbstractPersistenceModule;
import com.google.sitebricks.persist.Persister;

import java.lang.annotation.Annotation;

/**
 * A simple disk-based object store. Will use any file system directory
 * specified (must be currently mounted).
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class DiskModule extends AbstractPersistenceModule {
  private final String directory;

  public DiskModule(String directory) {
    this(null, directory);
  }

  public DiskModule(Class<? extends Annotation> selector, String directory) {
    super(selector);
    this.directory = directory;
  }

  @Override
  protected void internalConfigure() {
    DiskPersister persister = new DiskPersister(directory);
    requestInjection(persister);
    bind(Persister.class).toInstance(persister);
  }
}
