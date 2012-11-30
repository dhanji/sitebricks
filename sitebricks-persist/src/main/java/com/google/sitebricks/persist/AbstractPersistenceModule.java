package com.google.sitebricks.persist;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.Provider;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;

import javax.inject.Inject;
import javax.persistence.Entity;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.inject.matcher.Matchers.annotatedWith;

/**
 * Any Sitebricks persistence support module MUST subclass this module. It
 * provides many defaults and sanity checks.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public abstract class AbstractPersistenceModule extends PrivateModule {
  protected boolean autoStart = true;
  final Class<? extends Annotation> selector;
  private final Set<Class<?>> entityClasses = new LinkedHashSet<Class<?>>();

  public AbstractPersistenceModule(Class<? extends Annotation> selector) {
    this.selector = selector;
  }

  @Override
  protected final void configure() {
    configurePersistence();
    internalConfigure();

    // Bind the entity classes that have been registered in this persistence module.
    bind(new TypeLiteral<Set<Class<?>>>() {})
        .toInstance(Collections.unmodifiableSet(entityClasses));
    bind(EntityMetadata.class);

    // Do some validation of the subclass module.
    if (autoStart) {
      if (currentStage() == Stage.DEVELOPMENT)
        Logger.getLogger(AbstractPersistenceModule.class.getName())
            .warning("Auto-start is on, in DEVELOPMENT this is OK. But please make sure you disable" +
                " it in production. See http://sitebricks.org/autostart for more info.");
      else if (currentStage() == Stage.PRODUCTION)
        addError("Auto-start is on, this is not allowed in PRODUCTION. Please make sure you start" +
            " and stop the Persister manually. " +
            "See http://sitebricks.org/autostart for more info.");

      bind(AutoStart.class).asEagerSingleton();
    }

    // Make sure that certain bindings are available.
    requireBinding(Persister.class);
    final Key<Persister> persisterKey = selectorKey(Persister.class);
    Key<EntityStore> entityStoreKey = selectorKey(EntityStore.class);
    bind(entityStoreKey).toProvider(new Provider<EntityStore>() {
      @Inject Injector injector;

      @Override
      public EntityStore get() {
        return injector.getInstance(persisterKey).currentEntityStore();
      }
    });

    // Locally.
    if (selector != null)
      bind(persisterKey).to(Persister.class);
    expose(persisterKey);
    expose(entityStoreKey);
  }

  private static class AutoStart {
    @Inject
    private AutoStart(final Persister persister) {
      persister.start();

      // Schedule shutdown.
      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          persister.shutdown();
        }
      });
    }
  }

  /**
   * Disables automatic lifecycle management of the database. Generally, auto-start
   * is only advisable during development. In production you should always use
   * {@link Persister#start()} and {@link Persister#shutdown()} to manage the datastore's
   * lifecycle yourself.
   * <p>
   * If left enabled, Sitebricks will add a shutdown hook that cleans up the data store
   * before the JVM exits.
   */
  protected void disableAutoStart() {
    autoStart = false;
  }

  protected abstract void internalConfigure();

  /**
   * Called by subclasses to expose any interface or API. You MUST expose
   * via this method, in order for the correct datastore "selector" to be applied.
   * For example, this allows RedisModule to expose the <code>Jedis</code> artifact
   * without knowing the given database selector. In usage, then, a user can specify
   * their own annotation as a database selector:
   *
   * <pre>
   *  {@literal @}Inject {@literal @}TemporaryStore Jedis jedis;
   *
   *  {@literal @}Work {@literal @}TemporaryStore
   *   public void saveParameter() { .. }
   * </pre>
   */
  protected <T> void exposeToUser(Class<T> clazz) {
    Key<T> key = selectorKey(clazz);

    // Create a linked binding from the exposed annotation, then expose that link.
    if (selector != null)
      bind(key).to(clazz);
    expose(key);
  }

  <T> Key<T> selectorKey(Class<T> clazz) {
    Key<T> key;
    if (selector != null)
      key = Key.get(clazz, selector);
    else
      key = Key.get(clazz);
    return key;
  }

  @SuppressWarnings("unchecked")
  protected <T> void exposeEntityStoreDelegate(Class<T> clazz) {
    final Key<EntityStore> key = selectorKey(EntityStore.class);
    bind(clazz).toProvider(new Provider<T>() {
      @Inject
      Injector injector;

      @Override
      public T get() {
        return (T) injector.getInstance(key).delegate();
      }
    });

    exposeToUser(clazz);
  }

  protected void configurePersistence() {}

  /**
   * Adds an entity class for mapping into the current datastore. Each datastore has
   * its own semantics for mapping fields of a class into its particular storage format.
   * <p>
   * For example, JpaModule maps fields into SQL columns whereas RedisModule converts
   * an entire object into JSON for storage as a string. Customizing these semantics
   * is specific to each datastore. Consult the specific implementations docs for details.
   */
  protected final void addPersistent(Class<?> entity) {
    if (entityClasses.contains(entity)) {
      addError("Entity class was added more than once: "
          + entity.getName()
          + "; to the same persistence module: "
          + getClass().getSimpleName()
          + (selector == null ? "" : " => @" + selector.getSimpleName()));
      return;
    }

    entityClasses.add(entity);
  }

  /**
   * Scans all classes in the given package and its subpackages to discover
   * classes marked with <code>{@literal @}Entity</code>, which are then mapped
   * into this PersistenceModule for storage in the underlying datastore.
   * <p>
   *   This is the equivalent of calling {@link #addPersistent(Class)} for each
   *   mapped class in the given package tree.
   * <p>
   *   Note that classes not explicitly marked with the <code>{@literal @}Entity</code>
   *   will be ignored.
   */
  protected final void scan(Package tree) {
    Set<Class<?>> classes = Classes.matching(annotatedWith(Entity.class)).in(tree);
    for (Class<?> clazz : classes) {
      addPersistent(clazz);
    }
  }
}
