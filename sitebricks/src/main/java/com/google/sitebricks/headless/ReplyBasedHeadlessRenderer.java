package com.google.sitebricks.headless;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A renderer for pages that have no corresponding template, i.e. for headless
 * web services using the Reply.with() API.
 */
@Singleton
class ReplyBasedHeadlessRenderer implements HeadlessRenderer {
  private final Injector injector;

  @Inject
  public ReplyBasedHeadlessRenderer(Injector injector) {
    this.injector = injector;
  }

  public void render(HttpServletResponse response, Object o) throws IOException {
    // Guaranteed by Sitebrick's page validator.
    assert o instanceof Reply;

    Reply<?> reply = (Reply<?>)o;
    if (null == reply) {
      throw new RuntimeException("Sitebricks received a null reply from the resource.");
    }
    reply.populate(injector, response);
  }
}
