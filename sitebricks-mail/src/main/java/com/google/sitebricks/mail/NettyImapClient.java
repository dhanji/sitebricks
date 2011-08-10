package com.google.sitebricks.mail;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.sitebricks.mail.imap.*;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class NettyImapClient implements MailClient {
  private static final Logger log = LoggerFactory.getLogger(NettyImapClient.class);

  private final ExecutorService workerPool;
  private final ClientBootstrap bootstrap;

  private final MailClientConfig config;
  private final MailClientHandler mailClientHandler;

  private volatile Channel channel;
  private final AtomicLong sequence = new AtomicLong();

  private volatile Folder currentFolder = null;

  public NettyImapClient(MailClientPipelineFactory pipelineFactory,
                         MailClientConfig config,
                         MailClientHandler mailClientHandler,
                         ExecutorService bossPool,
                         ExecutorService workerPool) {
    this.workerPool = workerPool;
    this.bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
          bossPool,
          workerPool));

    this.config = config;
    this.mailClientHandler = mailClientHandler;
    this.bootstrap.setPipelineFactory(pipelineFactory);
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
    channel.write(". CAPABILITY\r\n");
    channel.write(". login " + config.getUsername() + " " + config.getPassword() + "\r\n");
    mailClientHandler.awaitLogin();
  }

  /**
   * Logs out of the current IMAP session and releases all resources, including
   * executor services.
   */
  @Override
  public void disconnect() {
    currentFolder = null;

    // Log out of the IMAP Server.
    channel.write(". logout\n");

    // Shut down all thread pools and exit.
    channel.close().awaitUninterruptibly(config.getTimeout(), TimeUnit.MILLISECONDS);
    bootstrap.releaseExternalResources();

    // TODO Shut down thread pools?
  }

  <D> ChannelFuture send(Command command, String args, SettableFuture<D> valueFuture) {
    Long seq = sequence.incrementAndGet();

    String commandString = seq + " " + command.toString()
        + (null == args ? "" : " " + args)
        + "\r\n";

    // Log the command but clip the \r\n
    log.debug("Sending {} to server...", commandString.substring(0, commandString.length() - 2));

    // Enqueue command.
    mailClientHandler.enqueue(new CommandCompletion(command, seq, valueFuture));

    return channel.write(commandString);
  }

  @Override
  public List<String> capabilities() {
    return mailClientHandler.getCapabilities();
  }

  @Override
  public ListenableFuture<List<String>> listFolders() {
    SettableFuture<List<String>> valueFuture = SettableFuture.create();

    send(Command.LIST_FOLDERS, "\"[Gmail]\" \"*\"", valueFuture);

    return valueFuture;
  }

  @Override
  public ListenableFuture<FolderStatus> statusOf(String folder) {
    SettableFuture<FolderStatus> valueFuture = SettableFuture.create();

    String args = '"' + folder + "\" (UIDNEXT RECENT MESSAGES UNSEEN)";
    send(Command.FOLDER_STATUS, args, valueFuture);

    return valueFuture;
  }

  @Override
  public ListenableFuture<Folder> open(String folder) {
    final SettableFuture<Folder> valueFuture = SettableFuture.create();
    valueFuture.addListener(new Runnable() {
      @Override
      public void run() {
        try {
          currentFolder = valueFuture.get();
        } catch (InterruptedException e) {
          log.error("Interrupted while attempting to open a folder", e);
        } catch (ExecutionException e) {
          log.error("Execution exception while attempting to open a folder", e);
        }
      }
    }, workerPool);

    String args = '"' + folder + "\"";
    send(Command.FOLDER_OPEN, args, valueFuture);

    return valueFuture;
  }

  @Override
  public ListenableFuture<List<MessageStatus>> list(Folder folder, int start, int end) {
    checkCurrentFolder(folder);
    Preconditions.checkArgument(start <= end, "Start must be <= end");
    Preconditions.checkArgument(start > 0, "Start must be greater than zero (IMAP uses 1-based " +
        "indexing)");
    SettableFuture<List<MessageStatus>> valueFuture = SettableFuture.create();

    String args = start + ":" + end + " all";
    send(Command.FETCH_HEADERS, args, valueFuture);

    return valueFuture;
  }

  @Override
  public ListenableFuture<List<Message>> fetch(Folder folder, int start, int end) {
    checkCurrentFolder(folder);
    Preconditions.checkArgument(start <= end, "Start must be <= end");
    Preconditions.checkArgument(start > 0, "Start must be greater than zero (IMAP uses 1-based " +
        "indexing)");
    SettableFuture<List<Message>> valueFuture = SettableFuture.create();

    String args = start + ":" + end + " all";
    send(Command.FETCH_FULL, args, valueFuture);

    return valueFuture;
  }

  @Override
  public void watch(Folder folder, FolderObserver observer) {
    checkCurrentFolder(folder);

    send(Command.IDLE, null, SettableFuture.<Object>create());

    mailClientHandler.observe(observer);
  }

  @Override
  public void unwatch() {
    // Stop watching folders.
    mailClientHandler.observe(null);

    channel.write(". DONE");
  }

  private void checkCurrentFolder(Folder folder) {
    Preconditions.checkState(folder.equals(currentFolder), "You must have opened folder %s" +
        " before attempting to read from it (%s is currently open).", folder.getName(),
        (currentFolder == null ? "No folder" : currentFolder.getName()));
  }
}
