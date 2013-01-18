package com.google.sitebricks.persist.redis;

import com.google.sitebricks.persist.EntityQuery;
import com.google.sitebricks.persist.EntityStore;
import redis.clients.jedis.Jedis;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class JedisEntityStore extends EntityStore {
  private final Jedis jedis;

  JedisEntityStore(Jedis jedis) {
    this.jedis = jedis;
  }

  @Override
  public <T> void remove(Class<T> type, Serializable key) {
    if (type != Parameter.class)
      throw new RuntimeException("Can only persist objects of type Parameter into Redis store");
    jedis.del(key.toString());
  }

  @Override
  public <T> Serializable save(T t) {
    // Jedis store only works with Parameter types for now.
    if (!(t instanceof Parameter))
      throw new RuntimeException("Can only persist objects of type Parameter into Redis store");

    Parameter parameter = (Parameter) t;
    jedis.set(parameter.name, parameter.value);

    return parameter.name;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T find(Class<T> clazz, Serializable key) {
    if (clazz != Parameter.class)
      throw new RuntimeException("Can only persist objects of type Parameter into Redis store");
    String keyString = key.toString();
    String value = jedis.get(keyString);
    if (value == null)
      return null;

    return (T) new Parameter(keyString, value);
  }

  @Override public <T> List<T> all(Class<T> type) {
    throw new UnsupportedOperationException("Bulk query of all values is not supported by this datastore");
  }

  @Override
  protected <T> List<T> execute(Class<T> type, Map<String, EntityQuery.FieldMatcher<?>> query,
                                int offset, int limit) {
    throw new UnsupportedOperationException("EntityStore query-API is not supported by this datastore");
  }

  @Override
  protected <T> void executeDelete(Class<T> type, Map<String, EntityQuery.FieldMatcher<?>> matcherMap) {
    throw new UnsupportedOperationException("Bulk deletion is not supported by this datastore");
  }

  @Override
  public Object delegate() {
    return jedis;
  }
}
