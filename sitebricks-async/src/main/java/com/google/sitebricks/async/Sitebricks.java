package com.google.sitebricks.async;

import java.util.concurrent.ExecutorService;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public interface Sitebricks {
  Sitebricks at(String host, int port);

  Sitebricks executors(ExecutorService bossPool, ExecutorService workerPool);

  /**
   * Synchronous start method.
   */
  void start();

  /**
   * Synchronous shutdown method.
   */
  void shutdown();
}
