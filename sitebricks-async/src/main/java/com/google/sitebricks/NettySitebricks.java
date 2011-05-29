package com.google.sitebricks;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class NettySitebricks implements Sitebricks {
  private ExecutorService bossPool;
  private ExecutorService workerPool;
  private ServerBootstrap bootstrap;

  private final Config config = new Config();
  private final WebSocketHandler handler;

  @Inject
  public NettySitebricks(WebSocketHandler handler) {
    this.handler = handler;
  }

  @Override
  public Sitebricks executors(ExecutorService bossPool, ExecutorService workerPool) {
    Preconditions.checkArgument(bossPool != null, "Boss executor cannot be null!");
    Preconditions.checkArgument(workerPool != null, "Worker executor cannot be null!");
    this.bossPool = bossPool;
    this.workerPool = workerPool;
    return this;
  }

  @Override
  public Sitebricks enableWebsockets(String uri) {
    config.websocketsUri = uri;
    return this;
  }

  @Override
  public Sitebricks enableWebsockets() {
    return enableWebsockets("/websocket");
  }

  @Override public Sitebricks at(String host, int port) {
    config.host = host;
    config.port = port;
    return this;
  }

  @Override public synchronized void start() {
    if (null == bossPool) {
      bossPool = Executors.newCachedThreadPool();
      workerPool = Executors.newCachedThreadPool();
    }

    // Start sitebricks.
    handler.startup(config);

    // Start Netty.
    this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossPool, workerPool));
    this.bootstrap.setPipelineFactory(new SitebricksWebsocketPipelineFactory(config, handler));
    this.bootstrap.bind(new InetSocketAddress(config.host, config.port));
  }

  @Override public synchronized void shutdown() {
    // This should shut down the threadpools too.
    bootstrap.releaseExternalResources();

    handler.shutdown();
  }
}
