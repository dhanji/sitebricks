package com.google.sitebricks.persist.sql;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.persist.EntityStore;
import com.google.sitebricks.persist.PersistAopModule;
import com.google.sitebricks.persist.Persister;
import com.google.sitebricks.persist.Transactional;
import com.google.sitebricks.persist.Work;
import com.jolbox.bonecp.BoneCPConfig;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SqlStoreIntegrationTest {
  public static final String A_NAME = "Jason Van Zyl";
  private static final String TEST_DB_FILE = "target/test_sql_db";
  private static final String ANOTHER_NAME = "Jason";

  public static class SqlSaver {
    @Inject
    Provider<Sql> sql;

    @Work
    public void make() {
      sql.get().execute("insert into my_table (id, name) values (1, @name)",
          ImmutableMap.<String, Object>of("name", A_NAME));
    }

    @Work
    public String find() {
      List<Map<String,Object>> list = sql.get().list("select * from my_table");
      assertFalse(list.isEmpty());
      return list.iterator().next().get("name").toString();
    }
  }

  public static class SqlTransactionalSaver {
    @Inject
    Provider<Sql> sql;

    @Work @Transactional
    public void make() {
      sql.get().execute("insert into my_table (id, name) values (1, @name)",
          ImmutableMap.<String, Object>of("name", ANOTHER_NAME));
    }

    @Work @Transactional
    public String find() {
      return sql.get().list("select * from my_table").iterator().next().get("name").toString();
    }
  }

  private static final AtomicInteger dbCount = new AtomicInteger(1);
  private BoneCPConfig config;

  @BeforeMethod
  public final void pre() {
    String database = "db" + dbCount.incrementAndGet();

    config = new BoneCPConfig();
    config.setJdbcUrl("jdbc:hsqldb:mem:" + database + ";sql.syntax_mys=true");
    config.setUser("sa");
    config.setPassword("");
  }

  private static void createTable(Injector injector) {
    Persister persister = injector.getInstance(Persister.class);
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        ((Sql) es.delegate()).execute("create table my_table (id integer, name text not null)");
        return null;
      }
    });

    final AtomicBoolean tableExists = new AtomicBoolean();
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        tableExists.set(((Sql) es.delegate()).tableExists("my_table"));
        return null;
      }
    });

    assertTrue(tableExists.get());
  }

  @Test
  public final void storeAndRetrieve() throws InterruptedException {
    SqlModule redisModule = new SqlModule(config);
    Injector injector = Guice.createInjector(redisModule, new PersistAopModule(redisModule));
    createTable(injector);
    SqlSaver saver = injector.getInstance(SqlSaver.class);

    saver.make();

    assertEquals(A_NAME, saver.find());
  }


  @Test
  public final void storeAndRetrieveTransactional() {
    SqlModule redisModule = new SqlModule(config);
    Injector injector = Guice.createInjector(redisModule, new PersistAopModule(redisModule));
    createTable(injector);
    SqlTransactionalSaver saver = injector
        .getInstance(SqlTransactionalSaver.class);

    saver.make();

    assertEquals(ANOTHER_NAME, saver.find());
  }

  @Test
  public final void storeAndRetrieveWithoutAop() {
    Injector injector = Guice.createInjector(new SqlModule(config));
    createTable(injector);
    Persister persister = injector
        .getInstance(Persister.class);

    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        ((Sql)es.delegate()).execute("insert into my_table (id, name) values (1, @name)",
            ImmutableMap.<String, Object>of("name", A_NAME));

        return null;
      }
    });

    Object param = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        return ((Sql)es.delegate()).list("select * from my_table").iterator().next().get("name").toString();
      }
    });

    assertEquals(A_NAME, param);
  }

  @Test
  public final void storeAndRemoveWithoutAop() {
    Injector injector = Guice.createInjector(new SqlModule(config));
    createTable(injector);
    Persister persister = injector.getInstance(Persister.class);

    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        ((Sql)es.delegate()).execute("insert into my_table (id, name) values (1, @name)",
            ImmutableMap.<String, Object>of("name", A_NAME));

        return null;
      }
    });

    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        ((Sql)es.delegate()).execute("delete from my_table where name = @name and id = @id",
            ImmutableMap.<String, Object>of(
                "id", 1,
                "name", A_NAME
            ));

        return null;
      }
    });

    Object param = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        return ((Sql)es.delegate()).list("select * from my_table");
      }
    });

    assertTrue(param instanceof List);
    assertTrue(((List)param).isEmpty());
  }

  @Test // No different in Redis to @Work
  public final void storeAndRetrieveInTransaction() {
    Injector injector = Guice.createInjector(new SqlModule(config));
    createTable(injector);
    final Persister persister = injector
        .getInstance(Persister.class);

    Object param = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        persister.call(new Persister.InTransaction() {
          @Override
          public Object perform(EntityStore es) throws Throwable {
            ((Sql)es.delegate()).execute("insert into my_table (id, name) values (1, @name)",
                ImmutableMap.<String, Object>of("name", ANOTHER_NAME));

            return null;
          }
        });

        return persister.call(new Persister.InTransaction() {
          @Override
          public Object perform(EntityStore es) throws Throwable {
            return ((Sql)es.delegate()).list("select * from my_table").iterator().next().get("name").toString();
          }
        });
      }
    });

    assertEquals(ANOTHER_NAME, param);
  }
}
