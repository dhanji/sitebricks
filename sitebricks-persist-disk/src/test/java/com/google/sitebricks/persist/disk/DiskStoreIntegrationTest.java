package com.google.sitebricks.persist.disk;

import com.google.inject.Guice;
import org.apache.commons.io.FileUtils;
import com.google.sitebricks.persist.EntityStore;
import com.google.sitebricks.persist.Persister;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static com.google.sitebricks.persist.EntityQuery.FieldMatcher.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNull;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class DiskStoreIntegrationTest {

  public static final String STORE_DIR = "target/storedir";

  private Persister persister;

  @BeforeMethod
  public void pre() throws IOException {
    persister = Guice.createInjector(new DiskModule(STORE_DIR) {
      @Override
      protected void configurePersistence() {
        disableAutoStart();
        addPersistent(MyEntity.class);
        addPersistent(MyCompositeKeyEntity.class);
      }
    }).getInstance(Persister.class);

    persister.start();
  }

  @AfterMethod
  public void post() throws IOException {
    persister.shutdown();
    try {
      FileUtils.deleteDirectory(new File(STORE_DIR));
    } catch (IOException e) {
      e.printStackTrace();
      // Can't do much really.
    }
  }

  @Test
  public final void storeAndRetrieve() {
    final Integer id = 6;
    final MyEntity myEntity = new MyEntity();
    myEntity.setId(id);
    myEntity.setAge(123);
    myEntity.setName("Aspod812joijas");
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(myEntity);
        return null;
      }
    });

    Object found = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        return es.find(MyEntity.class, id);
      }
    });

    assertEquals(found, myEntity);
  }

  @Test
  public final void storeAndRemove() {
    final Integer id = 6;
    final MyEntity myEntity = new MyEntity();
    myEntity.setId(id);
    myEntity.setAge(123);
    myEntity.setName("Aspod812joijas");
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(myEntity);
        return null;
      }
    });

    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.remove(MyEntity.class, id);
        return null;
      }
    });

    Object found = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        return es.find(MyEntity.class, id);
      }
    });

    assertNull(found);
  }

  @Test
  public final void storeAndRetrieveMultiple() {
    final Integer id = 6;
    final MyEntity myEntity = new MyEntity();
    myEntity.setId(id);
    myEntity.setAge(123);
    myEntity.setName("Aspod812joijas");
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(myEntity);
        myEntity.setId(7);
        es.save(myEntity);
        return null;
      }
    });

    Object found = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        return es.find(MyEntity.class, id + 1);
      }
    });

    assertEquals(found, myEntity);
  }

  @Test
  public final void storeAndRetrieveCompositeKey() {
    final MyCompositeKeyEntity myEntity = new MyCompositeKeyEntity();
    final MyCompositeKeyEntity.CompositeKey key = new MyCompositeKeyEntity.CompositeKey();
    key.setKeyPart1("pokqspokda");
    key.setKeyPart2("01012930");

    final MyCompositeKeyEntity.CompositeKey key2 = new MyCompositeKeyEntity.CompositeKey();
    key2.setKeyPart1("asijdia");
    key2.setKeyPart2("aopskdpok");

    myEntity.setId(key);
    myEntity.setAge(123);
    myEntity.setName("Aspod812joijas");
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(myEntity);
        myEntity.setId(key2);
        es.save(myEntity);
        return null;
      }
    });

    Object found = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        return es.find(MyCompositeKeyEntity.class, key2);
      }
    });

    assertEquals(found, myEntity);
  }

  @Test
  public final void storeAndQuery() {
    final Integer id = 6;
    final MyEntity myEntity = new MyEntity();
    myEntity.setId(id);
    myEntity.setAge(123);
    myEntity.setName("Jason");
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(myEntity);
        myEntity.setId(15);
        es.save(myEntity);
        return null;
      }
    });

    Object found = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        MyEntity my = es.topic(MyEntity.class);

        return es.from(my)
            .where(my.getName(), is("Jason"))
            .and(my.getId(), below(10))
            .list();
      }
    });

    assertNotNull(found);

    assertTrue(found instanceof Collection);
    @SuppressWarnings("unchecked") // by above check.
    Collection<MyEntity> results = (Collection<MyEntity>) found;
    assertEquals(results.size(), 1);

    // Compare with old entity.
    myEntity.setId(id);
    assertEquals(results.iterator().next(), myEntity);
  }

  @Test
  public final void storeAndQueryLike() {
    final Integer id = 6;
    final MyEntity myEntity = new MyEntity();
    myEntity.setId(id);
    myEntity.setAge(123);
    myEntity.setName("Jason");
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(myEntity);
        myEntity.setId(15);
        es.save(myEntity);
        return null;
      }
    });

    Object found = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        MyEntity my = es.topic(MyEntity.class);

        return es.from(my)
            .where(my.getName(), like("Ja"))
            .and(my.getId(), below(100))
            .list();
      }
    });

    assertNotNull(found);

    assertTrue(found instanceof Collection);
    @SuppressWarnings("unchecked") // by above check.
    Collection<MyEntity> results = (Collection<MyEntity>) found;
    assertEquals(results.size(), 2);

    for (MyEntity result : results) {
      assertEquals(result.getName(), myEntity.getName());
    }
  }

  @Test
  public final void storeAndQueryBetweenText() {
    final Integer id = 6;
    final MyEntity myEntity = new MyEntity();
    myEntity.setId(id);
    myEntity.setAge(123);
    myEntity.setName("Jason");
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(myEntity);
        myEntity.setId(15);
        es.save(myEntity);
        return null;
      }
    });

    Object found = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        MyEntity my = es.topic(MyEntity.class);

        return es.from(my)
            .where(my.getName(), between("Aida", "Zaxon"))
            .and(my.getId(), below(100))
            .list();
      }
    });

    assertNotNull(found);

    assertTrue(found instanceof Collection);
    @SuppressWarnings("unchecked") // by above check.
    Collection<MyEntity> results = (Collection<MyEntity>) found;
    assertEquals(results.size(), 2);

    for (MyEntity result : results) {
      assertEquals(result.getName(), myEntity.getName());
    }
  }

  @Test
  public final void storeAndQueryFuzzyString() {
    final Integer id = 6;
    final MyEntity myEntity = new MyEntity();
    myEntity.setId(id);
    myEntity.setAge(123);
    myEntity.setName("Jason");
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(myEntity);
        myEntity.setId(15);
        myEntity.setName("Poopy");
        es.save(myEntity);
        return null;
      }
    });

    Object found = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        MyEntity my = es.topic(MyEntity.class);

        return es.from(my)
            .where(my.getName(), similarTo("Jasin", 0.5))
            .and(my.getId(), below(100))
            .list();
      }
    });

    assertNotNull(found);

    assertTrue(found instanceof Collection);
    @SuppressWarnings("unchecked") // by above check.
    Collection<MyEntity> results = (Collection<MyEntity>) found;
    assertEquals(results.size(), 1);

    for (MyEntity result : results) {
      assertEquals(result.getName(), "Jason");
    }
  }

  @Test
  public final void storeAndDeleteWithQuery() {
    final Integer id = 6;
    final MyEntity myEntity = new MyEntity();
    myEntity.setId(id);
    myEntity.setAge(123);
    myEntity.setName("Jason");
    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        es.save(myEntity);
        myEntity.setId(15);
        es.save(myEntity);
        return null;
      }
    });

    persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        MyEntity my = es.topic(MyEntity.class);

        es.from(my)
            .where(my.getName(), similarTo("Jasin", 0.5))
            .remove();
        return null;
      }
    });

    Object found = persister.call(new Persister.InWork() {
      @Override
      public Object perform(EntityStore es) throws Throwable {
        MyEntity my = es.topic(MyEntity.class);

        return es.from(my)
            .where(my.getName(), similarTo("Jasin", 0.5))
            .list();
      }
    });

    assertNull(found);
  }
}
