package com.google.sitebricks;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletScopes;
import com.google.sitebricks.headless.NettyReplyMaker;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.routing.RoutingDispatcher;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class WebSocketHandler extends SimpleChannelUpstreamHandler {
  private static final Key<HttpRequest> HTTP_REQUEST_KEY = Key.get(HttpRequest.class);
  private final RoutingDispatcher dispatcher;
  private final Provider<Bootstrapper> bootstrapper;
  private final Provider<Shutdowner> teardowner;
  private final Provider<Request> requestProvider;
  private final NettyReplyMaker replyMaker;

  @Inject
  WebSocketHandler(RoutingDispatcher dispatcher, Provider<Bootstrapper> bootstrapper,
                   Provider<Shutdowner> teardowner,
                   Provider<Request> requestProvider,
                   NettyReplyMaker replyMaker) {
    this.dispatcher = dispatcher;
    this.bootstrapper = bootstrapper;
    this.teardowner = teardowner;
    this.replyMaker = replyMaker;
    this.requestProvider = requestProvider;
  }

  private Config config;

  public void startup(Config config) {
    this.config = config;
    bootstrapper.get().start();
  }

  public void shutdown() {
    teardowner.get().shutdown();
  }

  @Override
  public void messageReceived(final ChannelHandlerContext ctx, MessageEvent e)
      throws Exception {
    Object message = e.getMessage();
    if (message instanceof HttpRequest) {
      // Handle normal request.
      HttpRequest request = (HttpRequest) message;

      // Scope this request using Guice thread-local scopes.
      Map<Key<?>,Object> seedMap = Maps.newHashMap();
      seedMap.put(HTTP_REQUEST_KEY, request);

      ServletScopes.scopeRequest(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
              handleHttpRequest(ctx);
              return null;
            }
          }, seedMap).call();

    } else if (message instanceof WebSocketFrame) {
      // Handle websocket frame.
    }
  }

  private void handleHttpRequest(ChannelHandlerContext ctx) throws IOException {
    // Because this method executes within a request scope, we can obtain the Sitebricks
    // request directly from its provider.
    Object respondObject = dispatcher.dispatch(this.requestProvider.get());

    //was there any matching page? (if it was a headless response, we don't need to do anything).
    // Also we do not do anything if the page elected to do nothing.
    if (null != respondObject) {

      // Only use the string rendering pipeline if this is not a headless request.
      if (respondObject instanceof Respond) {
        Respond respond = (Respond) respondObject;

        //do we need to redirect or was this a successful render?
        final String redirect = respond.getRedirect();
        if (null != redirect) {
          // A redirect is called for...
          DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
              HttpResponseStatus.TEMPORARY_REDIRECT);
          response.setHeader(Names.LOCATION, redirect);
          ctx.getChannel().write(response);
        } else { //successful render

          // by checking if a content type was set, we allow users to override content-type
          //  on an arbitrary basis
          DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
              HttpResponseStatus.OK);
          response.setContent(ChannelBuffers.copiedBuffer(respond.toString(), CharsetUtil.UTF_8));
          response.setHeader(Names.CONTENT_TYPE, respond.getContentType());

          // Apparently you only need this for a keepAlive connection? Handle them...
          response.setHeader(Names.CONTENT_LENGTH, response.getContent().readableBytes());

          ctx.getChannel().write(response);
        }
      } else { // It must be a headless Reply. Render the headless response.
        assert respondObject instanceof Reply; // This just has to be true.
        ctx.getChannel().write(replyMaker.populate((Reply<Object>) respondObject));
      }
    } else {
      // Resource not found, reject (TODO we should serve static resources here from the file system)
      DefaultHttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
          HttpResponseStatus.NOT_FOUND);
      ctx.getChannel().write(response);
    }
  }
}
