package org.sitebricks.web;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.ReplyMaker;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.ServerCookieEncoder;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

/**
 * @author dhanji (Dhanji R. Prasanna)
 */
class HttpHandler extends SimpleChannelInboundHandler<Object> {
  private final Provider<NettyRequest> requestProvider;
  private final Injector injector;

  private HttpRequest request;
  /**
   * Buffer that stores the response content
   */
  private final StringBuilder buf = new StringBuilder();

  @Inject
  HttpHandler(Provider<NettyRequest> requestProvider, Injector injector) {
    this.requestProvider = requestProvider;
    this.injector = injector;
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
    if (msg instanceof HttpRequest) {
      HttpRequest request = this.request = (HttpRequest) msg;

      if (is100ContinueExpected(request)) {
        send100Continue(ctx);
      }

      NettyRequest nettyRequest = requestProvider.get();
      nettyRequest.set(request);

      // Dispatch PRE-phase here.
      // TODO ...
      buf.setLength(0);

      if (request.getDecoderResult().isFailure()) {
        //noinspection ThrowableResultOfMethodCallIgnored
        request.getDecoderResult().cause().printStackTrace(System.err);
      }
    }

    if (msg instanceof HttpContent) {
      HttpContent httpContent = (HttpContent) msg;

      ByteBuf content = httpContent.content();
      if (content.isReadable()) {
      }

      if (msg instanceof LastHttpContent) {
        // Dispatch ACTUAL-phase here.
        // TODO ...

        ReplyMaker<?> reply = (ReplyMaker<?>)Reply.with(ImmutableMap.of("hello", "there"));
        try {
          reply.populate(injector, ctx).addListener(f -> {
            // Dispatch AFTER_COMMIT-phase here.
            // TODO ...
            if (!isKeepAlive(request))
              ctx.close();
          });
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private static void send100Continue(ChannelHandlerContext ctx) {
    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
    ctx.write(response);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    cause.printStackTrace();
    ctx.close();
  }
}
