package org.sitebricks.web;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author dhanji (Dhanji R. Prasanna)
 */
public class NettyServer {
  private final Provider<HttpHandler> handlerProvider;

  @Inject
  public NettyServer(Provider<HttpHandler> handlerProvider) {
    this.handlerProvider = handlerProvider;
  }

  public void start(int port) {
    // Configure the server.
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      ServerBootstrap b = new ServerBootstrap();
      b.group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .childHandler(newInitializer());

      Channel ch = b.bind(port).sync().channel();
      System.out.println("Started on port " + port);

      ch.closeFuture().sync();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }

  private ChannelInitializer<SocketChannel> newInitializer() {
    return new ChannelInitializer<SocketChannel>() {
        @Override
        public void initChannel(SocketChannel ch) throws Exception {
                // Create a default pipeline implementation.
                ChannelPipeline p = ch.pipeline();

                // Uncomment the following line if you want HTTPS
                //SSLEngine engine = SecureChatSslContextFactory.getServerContext().createSSLEngine();
                //engine.setUseClientMode(false);
                //p.addLast("ssl", new SslHandler(engine));

                p.addLast("decoder", new HttpRequestDecoder());
                // Uncomment the following line if you don't want to handle HttpChunks.
                //p.addLast("aggregator", new HttpObjectAggregator(1048576));
                p.addLast("encoder", new HttpResponseEncoder());
                // Remove the following line if you don't want automatic content compression.
                //p.addLast("deflater", new HttpContentCompressor());

                p.addLast("streamer", new ChunkedWriteHandler());
                p.addLast("handler", handlerProvider.get());
            }
      };
  }
}
