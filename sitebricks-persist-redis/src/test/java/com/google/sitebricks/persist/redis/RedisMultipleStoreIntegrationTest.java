package com.google.sitebricks.persist.redis;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.sitebricks.persist.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.testng.AssertJUnit.*;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class RedisMultipleStoreIntegrationTest {

  public static class RedisTransactionalSaver {
    @Inject @StoreOne
    Provider<Jedis> jedisOne;
    @Inject @StoreTwo
    Provider<Jedis> jedisTwo;

    @Work @Transactional @StoreOne
    public void makeOne() {
      jedisOne.get().set("name", "Jason");
    }

    @Work @Transactional @StoreTwo
    public void makeTwo() {
      jedisTwo.get().set("name", "Maxine");
    }

    @Work @Transactional @StoreOne
    public String findOne() {
      return jedisOne.get().get("name");
    }

    @Work @Transactional @StoreTwo
    public String findTwo() {
      return jedisTwo.get().get("name");
    }
  }

//  @Test
  public final void storeAndRetrieveTransactional() {
    RedisModule redisModuleOne = new RedisModule(StoreOne.class, new JedisPoolConfig(), "localhost");
    RedisModule redisModuleTwo = new RedisModule(StoreTwo.class, new JedisPoolConfig(), "localhost:6380");
    RedisTransactionalSaver saver = Guice.createInjector(redisModuleOne, redisModuleTwo,
        new PersistAopModule(redisModuleOne),
        new PersistAopModule(redisModuleTwo))
        .getInstance(RedisTransactionalSaver.class);

    saver.makeOne();
    saver.makeTwo();

    assertEquals("Jason", saver.findOne());
    assertEquals("Maxine", saver.findTwo());
  }

//  @Test
  public final void storeAndRetrieveWithoutAop() {
    JedisPoolConfig configOne = new JedisPoolConfig();

    Injector injector = Guice.createInjector(
        new RedisModule(StoreOne.class, configOne, "localhost"),
        new RedisModule(StoreTwo.class, new JedisPoolConfig(), "localhost:6380"));

    Persister persisterOne = injector
        .getInstance(Key.get(Persister.class, StoreOne.class));

    Persister persisterTwo = injector
        .getInstance(Key.get(Persister.class, StoreTwo.class));

    assertNotSame(persisterOne, persisterTwo);
    assertFalse(persisterOne.equals(persisterTwo));

    // First DB
    persisterOne.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(new Parameter("name", "Jason Van Zyl"));

        return null;
      }
    });

    // Second DB
    persisterTwo.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(new Parameter("name", "Jason Zan Vyl"));

        return null;
      }
    });

    Object paramOne = persisterOne.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        return es.find(Parameter.class, "name");
      }
    });


    Object paramTwo = persisterTwo.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        return es.find(Parameter.class, "name");
      }
    });

    assertEquals("Jason Van Zyl", ((Parameter)paramOne).value);
    assertEquals("Jason Zan Vyl", ((Parameter)paramTwo).value);
  }
}
