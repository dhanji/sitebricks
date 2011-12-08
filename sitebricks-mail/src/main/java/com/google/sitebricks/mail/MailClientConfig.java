package com.google.sitebricks.mail;

import com.google.sitebricks.mail.Mail.Auth;
import com.google.sitebricks.mail.oauth.OAuthConfig;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MailClientConfig {
  private final String host;
  private final int port;
  private final Auth authType;
  private final String username;
  private final String password;
  private final long timeout;
  private final OAuthConfig oAuthConfig;

  private final boolean gmail;

  public MailClientConfig(String host, int port, Auth authType, String username, String password,
                          long timeout) {
    this.host = host;
    this.port = port;
    this.authType = authType;
    this.username = username;
    this.password = password;
    this.timeout = timeout;
    oAuthConfig = null;

    this.gmail = isGmail(host);
  }

  public MailClientConfig(String host,
                          int port,
                          String username,
                          OAuthConfig config,
                          long timeout) {
    this.host = host;
    this.port = port;
    this.authType = Auth.OAUTH;
    this.username = username;
    this.password = null;
    this.timeout = timeout;
    oAuthConfig = config;

    this.gmail = isGmail(host);
  }

  private static boolean isGmail(String host) {
    return host.contains("imap.gmail.com") || host.contains("imap.googlemail.com");
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }

  public Auth getAuthType() {
    return authType;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public long getTimeout() {
    return timeout;
  }

  public boolean useGmailExtensions() {
    return gmail;
  }

  public OAuthConfig getOAuthConfig() {
    return oAuthConfig;
  }
}
