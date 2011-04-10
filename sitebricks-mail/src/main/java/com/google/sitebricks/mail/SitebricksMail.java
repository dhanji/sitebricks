package com.google.sitebricks.mail;

import com.google.common.base.Preconditions;
import com.google.sitebricks.mail.Mail.AuthBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class SitebricksMail implements Mail, AuthBuilder {
  private String host;
  private int port;

  private long timeout;

  @Override
  public AuthBuilder clientOf(String host, int port) {
    Preconditions.checkArgument(null != host && !host.isEmpty(),
        "Must specify a valid hostname");
    Preconditions.checkArgument(port > 0,
        "Must specify a valid (non-zero) port");
    this.host = host;
    this.port = port;
    return this;
  }

  @Override
  public AuthBuilder timeout(long amount, TimeUnit unit) {
    this.timeout = unit.convert(amount, TimeUnit.MILLISECONDS);
    return this;
  }

  @Override
  public MailClient connect(Auth authType, String username, String password) {
    MailClientConfig config = new MailClientConfig(host, port, authType, username,
        password, timeout);
    MailClientHandler mailClientHandler = new MailClientHandler();
    MailClient client = new MailClient(new MailClientPipelineFactory(mailClientHandler), config,
        mailClientHandler);

    // Blocks until connected (timeout specified in config).
    client.connect();
    return client;
  }
}
