package com.google.sitebricks.async;

import com.google.common.base.Preconditions;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class NettySitebricks implements Sitebricks {
  private ExecutorService bossPool;
  private ExecutorService workerPool;
  private String host = "localhost";
  private int port = 8080;

  private ServerBootstrap bootstrap;

  @Override
  public Sitebricks executors(ExecutorService bossPool, ExecutorService workerPool) {
    Preconditions.checkArgument(bossPool != null, "Boss executor cannot be null!");
    Preconditions.checkArgument(workerPool != null, "Worker executor cannot be null!");
    this.bossPool = bossPool;
    this.workerPool = workerPool;
    return this;
  }

  @Override public Sitebricks at(String host, int port) {
    this.host = host;
    this.port = port;
    return this;
  }

  @Override public synchronized void start() {
    this.bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossPool, workerPool));
//    this.bootstrap.setPipelineFactory();
    this.bootstrap.bind(new InetSocketAddress(host, port));
  }

  @Override public synchronized void shutdown() {
    // This should shut down the threadpools too.
    bootstrap.releaseExternalResources();
  }
}
