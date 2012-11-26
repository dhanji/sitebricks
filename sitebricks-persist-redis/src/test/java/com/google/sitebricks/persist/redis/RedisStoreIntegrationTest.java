package com.google.sitebricks.persist.redis;

import com.google.inject.Guice;
import com.google.sitebricks.persist.*;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Date;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/**
 * This test needs redis to run so it is disabled in the nominal build.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class RedisStoreIntegrationTest {
  public static final String A_NAME = "Jason Van Zyl";

  public static class RedisSaver {
    @Inject
    Provider<Jedis> jedis;

    @Work
    public void make() {
      jedis.get().set("name", "Jason");
    }

    @Work
    public String find() {
      return jedis.get().get("name");
    }
  }

  public static class RedisTransactionalSaver {
    @Inject
    Provider<Jedis> jedis;

    @Work @Transactional
    public void make() {
      jedis.get().set("name", "Jason");
    }

    @Work @Transactional
    public String find() {
      return jedis.get().get("name");
    }
  }

//  @Test
  public final void storeAndRetrieve() {
    RedisModule redisModule = new RedisModule();
    RedisSaver saver = Guice.createInjector(redisModule, new PersistAopModule(redisModule))
        .getInstance(RedisSaver.class);

    saver.make();

    assertEquals("Jason", saver.find());
  }


//  @Test
  public final void storeAndRetrieveTransactional() {
    RedisModule redisModule = new RedisModule();
    RedisTransactionalSaver saver = Guice.createInjector(redisModule, new PersistAopModule(redisModule))
        .getInstance(RedisTransactionalSaver.class);

    saver.make();

    assertEquals("Jason", saver.find());
  }

//  @Test
  public final void storeAndRetrieveWithoutAop() {
    Persister persister = Guice.createInjector(new RedisModule())
        .getInstance(Persister.class);

    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(new Parameter("name", A_NAME));

        return null;
      }
    });

    Object param = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        return es.find(Parameter.class, "name");
      }
    });

    assertEquals(A_NAME, ((Parameter)param).value);
  }

//  @Test
  public final void storeAndRemoveWithoutAop() {
    Persister persister = Guice.createInjector(new RedisModule())
        .getInstance(Persister.class);
    final String aKey = "somekey_" + Long.toHexString(new Date().getTime());

    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(new Parameter(aKey, A_NAME));

        return null;
      }
    });

    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.remove(Parameter.class, aKey);
        return null;
      }
    });

    Object param = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        return es.find(Parameter.class, aKey);
      }
    });

    assertNull(param);
  }

//  @Test // No different in Redis to @Work
  public final void storeAndRetrieveInTransaction() {
    final Persister persister = Guice.createInjector(new RedisModule())
        .getInstance(Persister.class);

    Object param = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        persister.call(new Persister.InTransaction() {
          @Override
          public Object perform(EntityStore es) throws Throwable {
            es.save(new Parameter("name", A_NAME));

            return null;
          }
        });

        return persister.call(new Persister.InTransaction() {
          @Override
          public Object perform(EntityStore es) throws Throwable {
            return es.find(Parameter.class, "name");
          }
        });
      }
    });

    assertEquals(A_NAME, ((Parameter)param).value);
  }
}
