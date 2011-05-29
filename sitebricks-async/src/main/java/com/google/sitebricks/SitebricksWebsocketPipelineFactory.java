package com.google.sitebricks;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;


/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class SitebricksWebsocketPipelineFactory implements ChannelPipelineFactory {
  private final Config config;
  private final SitebricksAsyncHandler hander;

  public SitebricksWebsocketPipelineFactory(Config config, SitebricksAsyncHandler handler) {
    this.config = config;
    this.hander = handler;
  }

  @Override public ChannelPipeline getPipeline() throws Exception {
    // Create a default pipeline implementation.
    ChannelPipeline pipeline = Channels.pipeline();
    pipeline.addLast("decoder", new HttpRequestDecoder() {
      @Override
      public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        super.messageReceived(ctx,
            e);    //To change body of overridden methods use File | Settings | File Templates.
      }
    });
    pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
    pipeline.addLast("encoder", new HttpResponseEncoder());
    pipeline.addLast("handler", hander);
    return pipeline;
  }
}
