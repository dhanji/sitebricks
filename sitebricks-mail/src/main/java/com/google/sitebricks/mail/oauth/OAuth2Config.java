package com.google.sitebricks.mail.oauth;

import com.google.common.base.Preconditions;

/**
 * @author ilguzin@gmail.com (Denis A. Ilguzin http://youdev.co
 */
public class OAuth2Config {
  public volatile String accessToken;
  public final String clientId;
  public final String clientSecret;

  public OAuth2Config(String accessToken, String clientId, String clientSecret) {
    Preconditions.checkArgument(accessToken != null && !accessToken.isEmpty());
    Preconditions.checkArgument(clientId != null && !clientId.isEmpty());
    Preconditions.checkArgument(clientSecret != null && !clientSecret.isEmpty());

    this.accessToken = accessToken;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }
}
