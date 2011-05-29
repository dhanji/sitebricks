package com.google.sitebricks;

import com.google.inject.AbstractModule;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.util.Providers;
import com.google.sitebricks.headless.Request;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class SitebricksNettySupportModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(HttpRequest.class).toProvider(Providers.<HttpRequest>of(null)).in(RequestScoped.class);
    bind(Request.class).toProvider(NettyRequestProvider.class).in(RequestScoped.class);
  }
}
