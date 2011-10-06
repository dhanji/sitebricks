package com.google.sitebricks.mail;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.sitebricks.mail.imap.Command;
import com.google.sitebricks.mail.imap.Folder;
import com.google.sitebricks.mail.imap.FolderStatus;
import com.google.sitebricks.mail.imap.Message;
import com.google.sitebricks.mail.imap.MessageStatus;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
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
class NettyImapClient implements MailClient, Idler {
  private static final Logger log = LoggerFactory.getLogger(NettyImapClient.class);

  private final ExecutorService workerPool;
  private final ExecutorService bossPool;

  private final MailClientConfig config;

  // Connection variables.
  private volatile ClientBootstrap bootstrap;
  private volatile MailClientHandler mailClientHandler;

  // State variables:
  private final AtomicLong sequence = new AtomicLong();
  private volatile Channel channel;
  private volatile Folder currentFolder = null;
  private volatile boolean loggedIn = false;

  public NettyImapClient(MailClientConfig config,
                         ExecutorService bossPool,
                         ExecutorService workerPool) {
    this.workerPool = workerPool;
    this.bossPool = bossPool;
    this.config = config;
  }

  static {
    System.setProperty("mail.mime.decodetext.strict", "false");
  }

  public boolean isConnected() {
    return channel != null && channel.isConnected() && channel.isOpen();
  }

  private void reset() {
    Preconditions.checkState(!isConnected(),
        "Cannot reset while mail client is still connected (call disconnect() first).");

    // Just to be on the safe side.
    if (mailClientHandler != null)
      mailClientHandler.halt();

    this.mailClientHandler = new MailClientHandler(this);
    MailClientPipelineFactory pipelineFactory =
        new MailClientPipelineFactory(mailClientHandler, config);

    this.bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(bossPool, workerPool));
    this.bootstrap.setPipelineFactory(pipelineFactory);

    // Reset state (helps if this is a reconnect).
    this.currentFolder = null;
    this.sequence.set(0L);
    mailClientHandler.idling.set(false);
  }

  @Override
  public boolean connect() {
    return connect(null);
  }

  /**
   * Connects to the IMAP server logs in with the given credentials.
   */
  @Override
  public synchronized boolean connect(final DisconnectListener listener) {
    reset();

    ChannelFuture future = bootstrap.connect(new InetSocketAddress(config.getHost(),
        config.getPort()));

    Channel channel = future.awaitUninterruptibly().getChannel();
    if (!future.isSuccess()) {
      throw new RuntimeException("Could not connect channel", future.getCause());
    }

    this.channel = channel;
    if (null != listener) {
      // https://issues.jboss.org/browse/NETTY-47?page=com.atlassian.jirafisheyeplugin%3Afisheye-issuepanel#issue-tabs
      channel.getCloseFuture().addListener(new ChannelFutureListener() {
        @Override public void operationComplete(ChannelFuture future) throws Exception {
          listener.disconnected();
        }
      });
    }
    return login();
  }

  private boolean login() {
    channel.write(". CAPABILITY\r\n");
    channel.write(". login " + config.getUsername() + " " + config.getPassword() + "\r\n");
    return loggedIn = mailClientHandler.awaitLogin();
  }

  @Override public String lastError() {
    return mailClientHandler.lastError().error;
  }

  /**
   * Logs out of the current IMAP session and releases all resources, including
   * executor services.
   */
  @Override
  public synchronized void disconnect() {
    try {
      // If there is an error with the handler, dont bother logging out.
      if (!mailClientHandler.isHalted()) {
        Preconditions.checkState(!mailClientHandler.idling.get(),
            "Can't execute command while idling (are you watching a folder?)");

        // Log out of the IMAP Server.
        channel.write(". logout\n");
      }

      currentFolder = null;
    } finally {
      // Shut down all channels and exit (leave threadpools as is--for reconnects).
      // The Netty channel close listener will fire a disconnect event to our client,
      // automatically. See connect() for details.
      channel.close().awaitUninterruptibly(config.getTimeout(), TimeUnit.MILLISECONDS);
    }
  }

  <D> ChannelFuture send(Command command, String args, SettableFuture<D> valueFuture) {
    Long seq = sequence.incrementAndGet();

    String commandString = seq + " " + command.toString()
        + (null == args ? "" : " " + args)
        + "\r\n";

    // Log the command but clip the \r\n
    log.debug("Sending {} to server...", commandString.substring(0, commandString.length() - 2));

    // Enqueue command.
    mailClientHandler.enqueue(new CommandCompletion(command, seq, valueFuture, commandString));

    return channel.write(commandString);
  }

  @Override
  public List<String> capabilities() {
    return mailClientHandler.getCapabilities();
  }

  @Override
  // @Stateless
  public ListenableFuture<List<String>> listFolders() {
    Preconditions.checkState(loggedIn, "Can't execute command because client is not logged in");
    Preconditions.checkState(!mailClientHandler.idling.get(),
        "Can't execute command while idling (are you watching a folder?)");

    SettableFuture<List<String>> valueFuture = SettableFuture.create();

    // TODO Should we use LIST "[Gmail]" % here instead? That will only fetch top-level folders.
    send(Command.LIST_FOLDERS, "\"[Gmail]\" \"*\"", valueFuture);

    return valueFuture;
  }

  @Override
  // @Stateless
  public ListenableFuture<FolderStatus> statusOf(String folder) {
    Preconditions.checkState(loggedIn, "Can't execute command because client is not logged in");
    SettableFuture<FolderStatus> valueFuture = SettableFuture.create();

    String args = '"' + folder + "\" (UIDNEXT RECENT MESSAGES UNSEEN)";
    send(Command.FOLDER_STATUS, args, valueFuture);

    return valueFuture;
  }

  @Override
  public ListenableFuture<Folder> open(String folder) {
    return open(folder, false);
  }

  @Override
  // @Stateless
  public ListenableFuture<Folder> open(String folder, boolean readWrite) {
    Preconditions.checkState(loggedIn, "Can't execute command because client is not logged in");
    Preconditions.checkState(!mailClientHandler.idling.get(),
        "Can't execute command while idling (are you watching a folder?)");

    final SettableFuture<Folder> valueFuture = SettableFuture.create();
    final SettableFuture<Folder> externalFuture = SettableFuture.create();
    valueFuture.addListener(new Runnable() {
      @Override
      public void run() {
        try {
          // We do this to enforce a happens-before ordering between the time a folder is
          // saved to currentFolder and a listener registered by the user may fire in a parallel
          // executor service.
          currentFolder = valueFuture.get();
          externalFuture.set(currentFolder);
        } catch (InterruptedException e) {
          log.error("Interrupted while attempting to open a folder", e);
        } catch (ExecutionException e) {
          log.error("Execution exception while attempting to open a folder", e);
        }
      }
    }, workerPool);

    String args = '"' + folder + "\"";
    send(readWrite ? Command.FOLDER_OPEN : Command.FOLDER_EXAMINE, args, valueFuture);

    return externalFuture;
  }

  @Override
  public ListenableFuture<List<MessageStatus>> list(Folder folder, int start, int end) {
    Preconditions.checkState(loggedIn, "Can't execute command because client is not logged in");
    Preconditions.checkState(!mailClientHandler.idling.get(),
        "Can't execute command while idling (are you watching a folder?)");

    checkCurrentFolder(folder);
    checkRange(start, end);
    Preconditions.checkArgument(start > 0, "Start must be greater than zero (IMAP uses 1-based " +
        "indexing)");
    SettableFuture<List<MessageStatus>> valueFuture = SettableFuture.create();

    // -ve end range means get everything (*).
    String extensions = config.useGmailExtensions() ? " X-GM-MSGID X-GM-THRID X-GM-LABELS" : "";
    String args = start + ":" + toUpperBound(end) + " (RFC822.SIZE INTERNALDATE FLAGS ENVELOPE"
        + extensions + ")";
    send(Command.FETCH_HEADERS, args, valueFuture);

    return valueFuture;
  }

  private static void checkRange(int start, int end) {
    Preconditions.checkArgument(start <= end || end == -1, "Start must be <= end");
  }

  private static String toUpperBound(int end) {
    return (end > 0)
        ? Integer.toString(end)
        : "*";
  }

  @Override
  public ListenableFuture<List<Message>> fetch(Folder folder, int start, int end) {
    Preconditions.checkState(loggedIn, "Can't execute command because client is not logged in");
    Preconditions.checkState(!mailClientHandler.idling.get(),
        "Can't execute command while idling (are you watching a folder?)");

    checkCurrentFolder(folder);
    checkRange(start, end);
    Preconditions.checkArgument(start > 0, "Start must be greater than zero (IMAP uses 1-based " +
        "indexing)");
    SettableFuture<List<Message>> valueFuture = SettableFuture.create();

    String args = start + ":" + toUpperBound(end) + " body[]";
    send(Command.FETCH_BODY, args, valueFuture);

    return valueFuture;
  }

  @Override
  public synchronized void watch(Folder folder, FolderObserver observer) {
    Preconditions.checkState(loggedIn, "Can't execute command because client is not logged in");
    checkCurrentFolder(folder);
    Preconditions.checkState(mailClientHandler.idling.compareAndSet(false, true), "Already idling...");

    // This MUST happen in the following order, otherwise send() may trigger a new mail event
    // before we've registered the folder observer.
    mailClientHandler.observe(observer);
    channel.write(sequence.incrementAndGet() + " idle\r\n");
  }

  @Override
  public synchronized void unwatch() {
    if (!mailClientHandler.idling.get())
      return;

    // Stop watching folders. It's OK to null this early, because any interleaving new mail
    // events will just be dropped on the floor if there is no folder observer.
    mailClientHandler.observe(null);
    channel.write("done\r\n");
  }

  public synchronized void done() {
    log.trace("Dropping out of IDLE...");
    channel.write("done\r\n");
  }

  private void checkCurrentFolder(Folder folder) {
    Preconditions.checkState(folder.equals(currentFolder), "You must have opened folder %s" +
        " before attempting to read from it (%s is currently open).", folder.getName(),
        (currentFolder == null ? "No folder" : currentFolder.getName()));
  }
}
