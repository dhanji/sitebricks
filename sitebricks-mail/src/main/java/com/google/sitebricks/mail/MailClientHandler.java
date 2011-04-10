package com.google.sitebricks.mail;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class MailClientHandler extends SimpleChannelHandler {
  private static final Logger log = LoggerFactory.getLogger(MailClientHandler.class);

  private final CountDownLatch loginComplete = new CountDownLatch(1);
  private volatile boolean isLoggedIn = false;

  @Override
  public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
    if (!isLoggedIn) {
      String message = e.getMessage().toString();
      if (message.matches("[.] OK .*@.* \\(Success\\)")) {
        log.debug("Authenticated!");
        isLoggedIn = true;
        loginComplete.countDown();
      }
    }
    log.debug("Message received [{}] from {}", e.getMessage(), e.getRemoteAddress());
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
    log.error("Exception caught!", e.getCause());
  }

  void awaitLogin() {
    try {
      loginComplete.await();
    } catch (InterruptedException e) {
      throw new RuntimeException("Interruption while awaiting server login", e);
    }
  }
}
