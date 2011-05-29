package com.google.sitebricks.async;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.sitebricks.Bootstrapper;
import com.google.sitebricks.Shutdowner;
import com.google.sitebricks.routing.RoutingDispatcher;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class WebSocketHandler extends SimpleChannelUpstreamHandler {
  private final RoutingDispatcher dispatcher;
  private final Provider<Bootstrapper> bootstrapper;
  private final Provider<Shutdowner> teardowner;

  @Inject
  WebSocketHandler(RoutingDispatcher dispatcher, Provider<Bootstrapper> bootstrapper,
                   Provider<Shutdowner> teardowner) {
    this.dispatcher = dispatcher;
    this.bootstrapper = bootstrapper;
    this.teardowner = teardowner;
  }

  private Config config;

  public void startup(Config config) {
    this.config = config;
    bootstrapper.get().start();
  }

  public void shutdown() {
    teardowner.get().shutdown();
  }

  @Override public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
      throws Exception {
    Object message = e.getMessage();
    if (message instanceof HttpRequest) {
      // Handle normal request.
      HttpRequest request = (HttpRequest) message;
//      dispatcher.dispatch()
      
    } else if (message instanceof WebSocketFrame) {
      // Handle websocket frame.
    }
  }
}
