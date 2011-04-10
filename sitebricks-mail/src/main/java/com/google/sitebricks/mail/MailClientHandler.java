package com.google.sitebricks.mail;

import com.google.common.collect.MapMaker;
import com.google.sitebricks.mail.imap.Command;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MailClientHandler extends SimpleChannelHandler {
  private static final Logger log = LoggerFactory.getLogger(MailClientHandler.class);
  public static final String CAPABILITY_PREFIX = "* CAPABILITY";

  private final CountDownLatch loginComplete = new CountDownLatch(2);
  private volatile boolean isLoggedIn = false;
  private volatile List<String> capabilities;

  private final ConcurrentMap<Command, CommandCompletion> completions = new MapMaker().makeMap();

  public void enqueue(Command command, CommandCompletion completion) {
    completions.put(command, completion);
  }

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    String message = e.getMessage().toString();
    log.debug("Message received [{}] from {}", e.getMessage(), e.getRemoteAddress());

    if (message.startsWith(CAPABILITY_PREFIX)) {
      log.debug("Capabilities received {}", message);
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

    complete(message);
  }

  private void complete(String message) {
    Command command = Command.response(message);
    if (command == null) {
      log.error("Could not find the command that the received message was a response to {}",
          message);
      return;
    }
    CommandCompletion completion = completions.remove(command);
    if (completion == null) {
      log.error("Could not find the completion for command {} (Was it ever issued?)", command);
      return;
    }

    completion.complete(message);
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
}
