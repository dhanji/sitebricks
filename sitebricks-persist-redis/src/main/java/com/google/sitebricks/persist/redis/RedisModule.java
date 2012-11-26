package com.google.sitebricks.persist.redis;

import com.google.inject.Provides;
import com.google.sitebricks.persist.AbstractPersistenceModule;
import com.google.sitebricks.persist.Persister;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;

/**
 * Support for Redis as a backend using the Sitebricks Persister framework.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class RedisModule extends AbstractPersistenceModule {
  private final JedisPoolConfig config;
  private final String host;

  public RedisModule() {
    this(null, new JedisPoolConfig(), "localhost");
  }

  public RedisModule(JedisPoolConfig config, String host) {
    this(null, config, host);
  }

  public RedisModule(Class<? extends Annotation> selector, JedisPoolConfig config, String host) {
    super(selector);
    this.config = config;
    this.host = host;
  }

  @Override
  protected final void internalConfigure() {
    exposeEntityStoreDelegate(Jedis.class);
  }

  @Provides
  @Singleton
  Persister providePersistenceService() {
    return new JedisPersister(config, host);
  }

  @Provides @Singleton
  JedisPool provideJedisPool(Persister persister) {
    return ((JedisPersister)persister).getPool();
  }
}
