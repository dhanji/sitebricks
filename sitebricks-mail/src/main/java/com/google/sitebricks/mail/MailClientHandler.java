package com.google.sitebricks.mail;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
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

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    String message = e.getMessage().toString();
    if (!isLoggedIn) {
      if (message.matches("[.] OK .*@.* \\(Success\\)")) {
        isLoggedIn = true;
        loginComplete.countDown();
      }
    }

    if (loginComplete.getCount() > 0 && message.startsWith(CAPABILITY_PREFIX)) {
      this.capabilities = Arrays.asList(
          message.substring(CAPABILITY_PREFIX.length() + 1).split("[ ]+"));
      loginComplete.countDown();
    }

    log.trace("Message received [{}] from {}", e.getMessage(), e.getRemoteAddress());
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
