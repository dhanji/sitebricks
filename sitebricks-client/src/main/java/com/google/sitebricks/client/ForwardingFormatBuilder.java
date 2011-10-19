package com.google.sitebricks.client;

import net.jcip.annotations.Immutable;

/**
 * A FormatBuilder which forwards all its method class to another Web.FormatBuilder. Subclasses should override one or more
 * methods to modify the behavior of the provided FormatBuilder.
 *
 * @author Miroslav Genov (mgenov@gmail.com)
 */
@Immutable
abstract class ForwardingFormatBuilder implements Web.FormatBuilder{

  private final Web.FormatBuilder builder;

  public ForwardingFormatBuilder(Web.FormatBuilder builder) {
    this.builder = builder;
  }

  @Override
  public <T> Web.ReadAsBuilder<T> transports(Class<T> clazz) {
    return builder.transports(clazz);
  }

  @Override
  public Web.FormatBuilder auth(Web.Auth auth, String username, String password) {
    return builder.auth(auth, username, password);
  }
}
