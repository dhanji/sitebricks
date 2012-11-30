package com.google.sitebricks.persist.sql;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.sitebricks.persist.EntityStore;
import com.google.sitebricks.persist.PersistAopModule;
import com.google.sitebricks.persist.Persister;
import com.google.sitebricks.persist.Work;
import com.jolbox.bonecp.BoneCPConfig;
import org.testng.annotations.BeforeMethod;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class SqlMultipleStoreIntegrationTest {
  public static final String A_NAME = "Jason Van Zyl";

  public static class SqlSaver {
    @Inject @Db1
    Provider<Sql> sql1;

    @Inject @Db2
    Provider<Sql> sql2;

    @Work @Db2
    public void make() {
      sql2.get().execute("insert into my_table (id, name) values (1, @name)",
          ImmutableMap.<String, Object>of("name", A_NAME));
    }

    @Work @Db1
    public void make1() {
      sql1.get().execute("insert into my_table (id, name) values (1, @name)",
          ImmutableMap.<String, Object>of("name", A_NAME));
    }

    @Work @Db2
    public String find() {
      List<Map<String,Object>> list = sql2.get().list("select * from my_table");
      assertFalse(list.isEmpty());
      return list.iterator().next().get("name").toString();
    }
  }

  private BoneCPConfig config1;
  private BoneCPConfig config2;

  @BeforeMethod
  public final void pre() {
    config1 = new BoneCPConfig();
    config1.setJdbcUrl("jdbc:hsqldb:mem:muldb1;sql.syntax_mys=true");
    config1.setUser("sa");
    config1.setPassword("");
    config1.setPartitionCount(1);
    config1.setMaxConnectionsPerPartition(2);

    config2 = new BoneCPConfig();
    config2.setJdbcUrl("jdbc:hsqldb:mem:muldb2;sql.syntax_mys=true");
    config2.setUser("sa");
    config2.setPassword("");
    config2.setPartitionCount(1);
    config2.setMaxConnectionsPerPartition(2);
  }

  private static void createTable(Injector injector, Class<? extends Annotation> selector) {
    Persister persister = injector.getInstance(Key.get(Persister.class, selector));
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

//  @Test
  public final void storeAndRetrieve() {
    SqlModule db1Module = new SqlModule(Db1.class, config1);
    SqlModule db2Module = new SqlModule(Db2.class, config2);

    Injector injector = Guice.createInjector(
        db2Module,
        db1Module,
        new PersistAopModule(db2Module),
        new PersistAopModule(db1Module));
    createTable(injector, Db1.class);
//    createTable(injector, Db2.class);

    SqlSaver saver = injector.getInstance(SqlSaver.class);

    saver.make();
    saver.make1();

    assertEquals(A_NAME, saver.find());
  }
}
