package com.google.sitebricks.persist;

import net.sf.cglib.proxy.Enhancer;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * An abstraction over a session to the persistence store. Either use this or
 * the datastore's session API directly. This is analogous to Hibernate's Session,
 * JPA's EntityManager, etc. It ensures that there is an active, dedicated logical
 * session to the database for the current threads.
 * <p>
 * Here is an example using the Redis module:
 * <pre><code>
 *   public class MyPersonSaver {
 *    {@literal @}Inject Provider&lt;EntityStore&gt; store;
 *
 *    {@literal @}Work
 *     public void makePerson() {
 *       Parameter p = new Parameter("name", "Jason");
 *
 *       store.get().save(p);
 *     }
 *   }
 * </code></pre>
 *
 * Notice the <code>@Work</code> annotation, which tells Sitebricks that the method
 * <code>makePerson()</code> will do work with a datastore. During this method Sitebricks
 * ensures that a logical session to the datastore is open and accessible via <code>EntityStore</code>.
 * <p>
 * If you wish to use the datastore's API directly, here is the above example showing how (in this
 * case we're using <code>Jedis</code> as the Redis client API):
 * <pre><code>
 *   public class MyPersonSaver {
 *    {@literal @}Inject Provider&lt;Jedis&gt; jedis;
 *
 *    {@literal @}Work
 *     public void makePerson() {
 *       jedis.get().set("name", "Jason");
 *     }
 *   }
 * </code></pre>
 *
 * Note that Sitebricks ensures that the session is closed correctly after the method exits, and
 * any resources held open are returned to the appropriate pools.
 *
 * <p>
 *
 *   The main advantage to using <code>EntityStore</code> is that you can plug the underlying
 *   datastore in and out relatively easily. But it also means you have to conform to using
 *   <code>EntityStore</code> and friends everywhere. If you don't want to you can safely use
 *   the API of any supported underlying datastore implementation instead.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public abstract class EntityStore {

  @Inject
  private EntityMetadata metadata;

  /**
   * Save an object to the entity store. This object must be an instance of a class
   * known to Sitebricks.
   */
  public abstract <T> void save(T t);

  /**
   * Delete a persistent object from the underlying datastore.
   */
  public abstract <T> void remove(Class<T> type, Serializable key);

  /**
   * Returns an instance of the found class represented by the (primary) key provided.
   * The key may be any serializable type supported by the specific entity store. If no
   * such key was found, this method returns null.
   */
  public abstract <T> T find(Class<T> type, Serializable key);

  /**
   * Create an entity query (a type-safe abstract object query) that can be used to
   * query items stored in the underlying datastore. The returned query is not threadsafe.
   */
  public <T> EntityQuery<T> from(T entityTopic) {
    return new TypesafeEntityQuery<T>(entityTopic, this);
  }

  /**
   * Creates a topic that can be used in an EntityQuery (see {@link #from} for details.
   */
  public <T> T topic(Class<T> entity) {
    EntityMetadata.EntityDescriptor descriptor = metadata.of(entity);

    @SuppressWarnings("unchecked") // Cast is guaranteed by enhancer.
    T proxy = (T) Enhancer.create(entity, new Class[] { TopicProxy.HasCalledFields.class },
        new TopicProxy(descriptor));
    return proxy;
  }

  /**
   * Datastore specific implementation of query execution.
   */
  protected abstract <T> List<T> execute(Class<T> type, Map<String, EntityQuery.FieldMatcher<?>> query, int offset, int limit);

  /**
   * Datastore specific implementation of bulk deletion query.
   */
  protected abstract <T> void executeDelete(Class<T> type,
                                            Map<String, EntityQuery.FieldMatcher<?>> matcherMap);

  /**
   * Returns the underlying implementation API (you should rarely ever need this).
   */
  public abstract Object delegate();


  /**
   * Returns the underlying implementation API (you should rarely ever need this).
   */
  public interface EntityTransaction {
    void commit();

    void rollback();
  }
}
