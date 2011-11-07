package com.google.sitebricks.mail.oauth;

import com.google.common.base.Preconditions;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class OAuthConfig {
  public volatile String accessToken;
  public volatile String tokenSecret;
  public final String clientId;
  public final String clientSecret;

  public OAuthConfig(String accessToken, String tokenSecret, String clientId, String clientSecret) {
    Preconditions.checkArgument(accessToken != null && !accessToken.isEmpty());
    Preconditions.checkArgument(tokenSecret != null && !tokenSecret.isEmpty());
    Preconditions.checkArgument(clientId != null && !clientId.isEmpty());
    Preconditions.checkArgument(clientSecret != null && !clientSecret.isEmpty());

    this.accessToken = accessToken;
    this.tokenSecret = tokenSecret;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }
}
