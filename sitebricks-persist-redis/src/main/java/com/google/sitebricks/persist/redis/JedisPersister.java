package com.google.sitebricks.persist.redis;

import com.google.sitebricks.persist.EntityStore;
import com.google.sitebricks.persist.Persister;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class JedisPersister extends Persister {
  private static final Pattern HOST_PORT_REGEX = Pattern.compile("(.*):(\\d+)");

  private final JedisPoolConfig config;
  private final String host;
  private final int port;

  private volatile JedisPool pool;

  private static final EntityStore.EntityTransaction SINK = new EntityStore.EntityTransaction() {
    @Override
    public void commit() {}

    @Override
    public void rollback() {}
  };

  JedisPersister(JedisPoolConfig config, String host) {
    this.config = config;
    int port = 6379;    // default redis port.

    Matcher matcher = HOST_PORT_REGEX.matcher(host);
    if (matcher.matches()) {
      host = matcher.group(1);
      port = Integer.parseInt(matcher.group(2));
    }

    this.host = host;
    this.port = port;
  }

  @Override
  public synchronized void start() {
    pool = new JedisPool(config, host, port);
  }

  @Override
  public synchronized void shutdown() {
    pool.destroy();
    pool = null;
  }

  JedisPool getPool() {
    return pool;
  }

  @Override
  protected EntityStore beginWork() {
    return new JedisEntityStore(pool.getResource());
  }

  @Override
  protected void endWork(EntityStore store, boolean commit /* ignored in Redis */) {
    pool.returnResource((Jedis) store.delegate());
  }

  @Override
  protected EntityStore.EntityTransaction beginTransaction() {
    return SINK;
  }
}
