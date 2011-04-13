package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ValueFuture;
import com.google.sitebricks.mail.imap.Command;
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
class NettyImapClient implements MailClient {
  private static final Logger log = LoggerFactory.getLogger(NettyImapClient.class);

  private final ClientBootstrap bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(
          Executors.newCachedThreadPool(),
          Executors.newCachedThreadPool()));

  private final MailClientConfig config;
  private final MailClientHandler mailClientHandler;

  private volatile Channel channel;

  public NettyImapClient(MailClientPipelineFactory pipelineFactory, MailClientConfig config,
                         MailClientHandler mailClientHandler) {
    this.config = config;
    this.mailClientHandler = mailClientHandler;
    bootstrap.setPipelineFactory(pipelineFactory);
  }

  /**
   * Connects to the IMAP server logs in with the given credentials.
   */
  @Override
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
    channel.write(". CAPABILITY\n");
    channel.write(". STARTTLS\n");
    channel.write(". login " + config.getUsername() + " " + config.getPassword() + "\n");
    mailClientHandler.awaitLogin();
  }

  /**
   * Logs out of the current IMAP session and releases all resources, including
   * executor services.
   */
  @Override
  public void disconnect() {
    // Log out of the IMAP Server.
    channel.write(". logout\n");

    // Shut down all thread pools and exit.
    channel.close().awaitUninterruptibly(config.getTimeout(), TimeUnit.MILLISECONDS);
    bootstrap.releaseExternalResources();
  }

  ChannelFuture send(final String command, ValueFuture<List<String>> valueFuture) {
    log.debug("Sending {} to server...", command);

    // Enqueue command.
    mailClientHandler.enqueue(Command.request(command), new CommandCompletion(valueFuture));

    return channel.write(command);
  }

  @Override
  public List<String> capabilities() {
    return mailClientHandler.getCapabilities();
  }

  @Override
  public ListenableFuture<List<String>> listFolders() {
    ValueFuture<List<String>> valueFuture = ValueFuture.create();

    send(". examine inbox\n", valueFuture);

    return valueFuture;
  }
}
