package com.google.sitebricks.mail;

import com.google.sitebricks.mail.Mail.Auth;

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

  public MailClientConfig(String host, int port, Auth authType, String username, String password,
                          long timeout) {
    this.host = host;
    this.port = port;
    this.authType = authType;
    this.username = username;
    this.password = password;
    this.timeout = timeout;
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
}
