package com.google.sitebricks;

import java.util.concurrent.ExecutorService;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface Sitebricks {
  /**
   * Specifies the host and port to listen for incoming HTTP requests on. Defaults
   * to {@code localhost:8080}
   */
  Sitebricks at(String host, int port);

  /**
   * The optional threadpools to use for processing requests internally. See
   * JBoss Netty for details on the boss executor and the worker executor.
   */
  Sitebricks executors(ExecutorService bossPool, ExecutorService workerPool);

  /**
   * Calling this method will enable HTML5 websockets support.
   *
   * @param uri The URI at which to listen for websocket connections.
   */
  Sitebricks enableWebsockets(String uri);

  /**
   * Identical to {@link #enableWebsockets} except that it uses the default uri
   * {@code /websocket}.
   */
  Sitebricks enableWebsockets();

  /**
   * Synchronous start method.
   */
  void start();

  /**
   * Synchronous shutdown method.
   */
  void shutdown();
}
