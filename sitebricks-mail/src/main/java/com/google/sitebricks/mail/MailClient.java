package com.google.sitebricks.mail;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MailClient {
  private static final Logger log = LoggerFactory.getLogger(MailClient.class);

  private static final String USERNAME = System.getProperty("sitebricks-mail.username");
  private static final String PASSWORD = System.getProperty("sitebricks-mail.password");
  public static final int WAIT_DELAY = 5;

  private static AtomicReference<Channel> channelRef = new AtomicReference<Channel>();

  public static void main(String[] args) throws Exception {
    // Parse options.
    String host = "imap.gmail.com";
    int port = 993;

    // Configure the client.
    ClientBootstrap bootstrap = new ClientBootstrap(
        new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(),
            Executors.newCachedThreadPool()));

    // Configure the pipeline factory.
    bootstrap.setPipelineFactory(new MailClientPipelineFactory());

    // Start the connection attempt.
    ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));

    // Wait until the connection attempt succeeds or fails.
    Channel channel = future.awaitUninterruptibly().getChannel();
    if (!future.isSuccess()) {
      future.getCause().printStackTrace();
      bootstrap.releaseExternalResources();
      return;
    }
    channelRef.set(channel);

    // Sends a command line to the server.
    send(". login " + USERNAME + " " + PASSWORD + "\n");

    // TODO(dhanji): Make this not necessary by chaining the callbacks together.
    Thread.sleep(5000L);
    // Wait until the server closes the connection.
    channel.close().awaitUninterruptibly(WAIT_DELAY, TimeUnit.SECONDS);

    // Close the connection.  Make sure the close operation ends because
    // all I/O operations are asynchronous in Netty.
    channel.close().awaitUninterruptibly();

    // Shut down all thread pools to exit.
    bootstrap.releaseExternalResources();
  }

  private static ExecutorService executor = Executors.newSingleThreadExecutor();

  static void send(final String command) {
    log.debug("Writing {}", command);

    executor.submit(new Runnable() {
      @Override
      public void run() {
        channelRef.get().write(command).awaitUninterruptibly(WAIT_DELAY, TimeUnit.SECONDS);
      }
    });
  }
}
