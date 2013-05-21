package com.google.sitebricks.ext;

import org.apache.commons.fileupload.FileItem;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.sitebricks.binding.MvelFileItemRequestBinder;
import com.google.sitebricks.binding.RequestBinder;

/**
 * Module encapsulates external bindings for sitebricks fileupload. Can be installed multiple times.
 */
public class SitebricksExtUploadModule extends AbstractModule {

  @Override
  protected void configure() {
      
    bind(new TypeLiteral<RequestBinder<FileItem>>(){}).to(MvelFileItemRequestBinder.class).asEagerSingleton();
  }

  @Override
  public int hashCode() {
    return SitebricksExtUploadModule.class.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return SitebricksExtUploadModule.class.isInstance(obj);
  }
}
