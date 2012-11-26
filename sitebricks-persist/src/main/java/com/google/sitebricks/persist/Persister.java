package com.google.sitebricks.persist;

/**
 * Persister Service that manages an entire data store or set of data
 * stores as identified by an annotation.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public abstract class Persister {
  public abstract void start();

  public abstract void shutdown();

  private final ThreadLocal<EntityStore> entityStoreThreadLocal =
      new ThreadLocal<EntityStore>();
  private final ThreadLocal<EntityStore.EntityTransaction> transactionThreadLocal =
      new ThreadLocal<EntityStore.EntityTransaction>();

  public Object call(final InTransaction t) {
    EntityStore entityStore = entityStoreThreadLocal.get();
    if (entityStore == null)
      throw new IllegalStateException("No unit of work is in progress! \n" +
          " Please call transactions inside an @Work or Persister.call(InWork...) block.\n" +
          " (Hint: You dont need transactions unless you specifically want to flush data\n" +
          " inside a single unit of work--use @Work or Persister.call(InWork...) instead)\n");

    EntityStore.EntityTransaction transaction = transactionThreadLocal.get();
    boolean shouldNotJoin = transaction == null;
    if (shouldNotJoin) {
      transaction = beginTransaction();
      transactionThreadLocal.set(transaction);
    }

    boolean shouldRollback = false;
    try {
      return t.perform(entityStore);
    } catch (Throwable throwable) {
      shouldRollback = true;
      if (throwable instanceof RuntimeException)
        throw (RuntimeException) throwable;

      throw new RuntimeException(throwable);
    } finally {

      // Only handle commit/rollback if we're not joining an outer transaction.
      if (shouldNotJoin) {
        try {
          if (shouldRollback)
            transaction.rollback();
          else
            transaction.commit();
        } finally {
          transactionThreadLocal.remove();
        }
      }
    }
  }

  public Object call(InWork inWork) {
    EntityStore entityStore = entityStoreThreadLocal.get();

    boolean shouldNotJoin = entityStore == null;
    boolean commit = true;
    if (shouldNotJoin) {
      entityStore = beginWork();
      entityStoreThreadLocal.set(entityStore);
    }
    try {
      return inWork.perform(entityStore);
    } catch (Throwable throwable) {
      commit = false;
      if (throwable instanceof RuntimeException)
        throw (RuntimeException) throwable;

      throw new RuntimeException(throwable);

    } finally {
      if (shouldNotJoin) {
        entityStoreThreadLocal.remove();
        endWork(entityStore, commit);
      }
    }
  }

  EntityStore currentEntityStore() {
    EntityStore entityStore = entityStoreThreadLocal.get();
    if (null == entityStore)
      throw new IllegalStateException("Entity store requested outside a unit of work!" +
          " Try injecting a Provider<EntityStore> if injecting into a Singleton. Or use" +
          " Persister.call(InWork...) instead.");
    return entityStore;
  }

  protected abstract EntityStore beginWork();

  protected abstract void endWork(EntityStore store, boolean commit);

  protected abstract EntityStore.EntityTransaction beginTransaction();

  public static interface InTransaction {
    Object perform(EntityStore es) throws Throwable;
  }

  public static interface InWork {
    Object perform(EntityStore es) throws Throwable;
  }
}
