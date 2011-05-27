package com.google.sitebricks.async;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class WebSocketHandler extends SimpleChannelUpstreamHandler {
  private static final String PATH = "/websocket";

  @Override public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
      throws Exception {
    Object message = e.getMessage();
    if (message instanceof HttpRequest) {
      // Handle normal request.
    } else if (message instanceof WebSocketFrame) {
      // Handle websocket frame.
    }
  }
}
