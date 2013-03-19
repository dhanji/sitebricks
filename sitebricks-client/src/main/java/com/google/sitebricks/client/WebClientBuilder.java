package com.google.sitebricks.client;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.sitebricks.client.Web.ReadAsBuilder;
import com.google.sitebricks.client.transport.Text;
import net.jcip.annotations.NotThreadSafe;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@NotThreadSafe
class WebClientBuilder implements Web.FormatBuilder {

  private final Injector injector;

  private String url;
  private Map<String, String> headers;

  private Web.Auth authType;
  private String username;
  private String password;
  private boolean usePreemptiveAuth;

    @Inject
  public WebClientBuilder(Injector injector) {
    this.injector = injector;
  }

  public Web.FormatBuilder clientOf(String url) {
    this.url = url;
    this.headers = null;

    return this;
  }

  public Web.FormatBuilder clientOf(String url, Map<String, String> headers) {
    this.url = url;
    this.headers = headers;

    return this;
  }

  public <T> Web.ReadAsBuilder<T> transports(Class<T> clazz) {
    return new InternalReadAsBuilder<T>(new TypeLiteral<T>() {});
  }

  @Override
  public <T> ReadAsBuilder<T> transports(TypeLiteral<T> clazz) {
    return new InternalReadAsBuilder<T>(clazz);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> WebClient<T> transportsText() {
    return (WebClient<T>) transports(String.class).over(Text.class);
  }

  public Web.FormatBuilder auth(Web.Auth auth, String username, String password) {
    Preconditions.checkArgument(null != auth, "Invalid auth type, null.");
    Preconditions.checkArgument(null != username, "Username cannot be null.");
    Preconditions.checkArgument(null != password, "Password cannot be null.");

    this.authType = auth;
    this.username = username;
    this.password = password;
    return this;
  }

  public Web.FormatBuilder auth(Web.Auth auth, String username, String password, boolean usePreemptiveAuth) {
    Preconditions.checkArgument(null != auth, "Invalid auth type, null.");
    Preconditions.checkArgument(null != username, "Username cannot be null.");
    Preconditions.checkArgument(null != password, "Password cannot be null.");

    this.authType = auth;
    this.username = username;
    this.password = password;
    this.usePreemptiveAuth = usePreemptiveAuth;
    return this;
  }

  private class InternalReadAsBuilder<T> implements Web.ReadAsBuilder<T> {
    private final TypeLiteral<T> transporting;

    private InternalReadAsBuilder(TypeLiteral<T> transporting) {
      this.transporting = transporting;
    }

    public WebClient<T> over(Class<? extends Transport> transport) {
      return new AHCWebClient<T>(injector, injector.getInstance(transport), authType, username, password, 
                                 usePreemptiveAuth, url, headers, transporting);
    }
  }
}
