package com.google.sitebricks.mail;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * A command/response handler for a single mail connection/user.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MailClientHandler extends SimpleChannelHandler {
  private static final Logger log = LoggerFactory.getLogger(MailClientHandler.class);
  public static final String CAPABILITY_PREFIX = "* CAPABILITY";

  private final CountDownLatch loginComplete = new CountDownLatch(2);
  private volatile boolean isLoggedIn = false;
  private volatile List<String> capabilities;
  private volatile FolderObserver observer;

  private final Queue<CommandCompletion> completions = new ConcurrentLinkedQueue<CommandCompletion>();

  public void enqueue(CommandCompletion completion) {
    completions.add(completion);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    String message = e.getMessage().toString();
    log.debug("Message received [{}] from {}", e.getMessage(), e.getRemoteAddress());

    if (message.startsWith(CAPABILITY_PREFIX)) {
      this.capabilities = Arrays.asList(
          message.substring(CAPABILITY_PREFIX.length() + 1).split("[ ]+"));
      loginComplete.countDown();
      return;
    }

    if (!isLoggedIn) {
      if (message.matches("[.] OK .*@.* \\(Success\\)")) {
        log.debug("Authentication success.");
        isLoggedIn = true;
        loginComplete.countDown();
      }
      // TODO handle auth failed
      return;
    }

    if (null != observer) {
      message = message.toLowerCase();
      if (message.endsWith("exists")) {
        observer.onMailAdded();
        return;
      } else if (message.endsWith("expunge")) {
        observer.onMailRemoved();
        return;
      }
    }

    complete(message);
  }

  private void complete(String message) {
    CommandCompletion completion = completions.peek();
    if (completion == null) {
      log.error("Could not find the completion for message {} (Was it ever issued?)", message);
      return;
    }

    if (completion.complete(message)) {
      completions.poll();
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    log.error("Exception caught!", e.getCause());
  }

  public List<String> getCapabilities() {
    return capabilities;
  }

  void awaitLogin() {
    try {
      loginComplete.await();
    } catch (InterruptedException e) {
      throw new RuntimeException("Interruption while awaiting server login", e);
    }
  }

  /**
   * Registers a FolderObserver to receive events happening with a particular
   * folder. Typically an IMAP IDLE feature. If called multiple times, will
   * overwrite the currently set observer.
   */
  void observe(FolderObserver observer) {
    this.observer = observer;
  }
}
