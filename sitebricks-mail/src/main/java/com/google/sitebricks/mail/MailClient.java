package com.google.sitebricks.mail;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MailClient {
  private static final Logger log = LoggerFactory.getLogger(MailClient.class);

  private final ClientBootstrap bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(
          Executors.newCachedThreadPool(),
          Executors.newCachedThreadPool()));

  private final MailClientConfig config;
  private final MailClientHandler mailClientHandler;

  private volatile Channel channel;

  public MailClient(MailClientPipelineFactory pipelineFactory, MailClientConfig config,
                    MailClientHandler mailClientHandler) {
    this.config = config;
    this.mailClientHandler = mailClientHandler;
    bootstrap.setPipelineFactory(pipelineFactory);
  }

  /**
   * Connects to the IMAP server logs in with the given credentials.
   */
  public void connect() {
    ChannelFuture future = bootstrap.connect(new InetSocketAddress(config.getHost(),
        config.getPort()));

    Channel channel = future.awaitUninterruptibly().getChannel();
    if (!future.isSuccess()) {
      bootstrap.releaseExternalResources();
      throw new RuntimeException("Could not connect channel", future.getCause());
    }

    this.channel = channel;
    login();
  }

  private void login() {
    send(". login " + config.getUsername() + " " + config.getPassword() + "\n");
    send(". CAPABILITY\n");
    mailClientHandler.awaitLogin();
  }

  /**
   * Logs out of the current IMAP session and releases all resources, including
   * executor services.
   */
  public void disconnect() {
    // Log out of the IMAP Server.
    send(". logout");

    // Shut down all thread pools and exit.
    channel.close().awaitUninterruptibly(config.getTimeout(), TimeUnit.MILLISECONDS);
    bootstrap.releaseExternalResources();
  }

  ChannelFuture send(final String command) {
    log.trace("Sending {} to server...", command);

    return channel.write(command);
  }

  public List<String> capabilities() {
    return mailClientHandler.getCapabilities();
  }
}
