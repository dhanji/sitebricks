package com.google.sitebricks.persist.redis;

import com.google.sitebricks.persist.EntityStore;
import com.google.sitebricks.persist.Persister;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class JedisPersister extends Persister {
  private static final Pattern HOST_PORT_REGEX = Pattern.compile("(.*):(\\d+)");

  private final JedisPoolConfig config;
  private final String host;
  private final String password;
  private final int port;

  private volatile JedisPool pool;

  private static final EntityStore.EntityTransaction SINK = new EntityStore.EntityTransaction() {
    @Override
    public void commit() {}

    @Override
    public void rollback() {}
  };

  JedisPersister(JedisPoolConfig config, String hostOrUri) {
    this.config = config;
    String host = hostOrUri;
    int port = 6379;    // default redis port.
    String password = null;

    // Discover password if necessary.
    URI uri = URI.create(hostOrUri);
    // If using the redis protocol, try to discover password
    if ("redis".equals(uri.getScheme())) {
      host = uri.getHost();
      port = uri.getPort();

      if (uri.getUserInfo() != null) {
        String[] split = uri.getUserInfo().split(":", 2);
        if (split.length > 1)
          password = split[1];
      }
    } else {
      Matcher matcher = HOST_PORT_REGEX.matcher(hostOrUri);
      if (matcher.matches()) {
        host = matcher.group(1);
        port = Integer.parseInt(matcher.group(2));
      }
    }

    this.host = host;
    this.port = port;
    this.password = password;
  }

  @Override
  public synchronized void start() {
    if (password == null)
      pool = new JedisPool(config, host, port);
    else
      pool = new JedisPool(config, host, port, Protocol.DEFAULT_TIMEOUT, password);
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
