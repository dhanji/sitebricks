package com.google.sitebricks;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;


/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class SitebricksWebsocketPipelineFactory implements ChannelPipelineFactory {
  private final Config config;
  private final WebSocketHandler hander;

  public SitebricksWebsocketPipelineFactory(Config config, WebSocketHandler handler) {
    this.config = config;
    this.hander = handler;
  }

  @Override public ChannelPipeline getPipeline() throws Exception {
    // Create a default pipeline implementation.
    ChannelPipeline pipeline = Channels.pipeline();
    pipeline.addLast("decoder", new HttpRequestDecoder());
    pipeline.addLast("aggregator", new HttpChunkAggregator(65536));
    pipeline.addLast("encoder", new HttpResponseEncoder());
    pipeline.addLast("handler", hander);
    return pipeline;
  }
}
