package com.google.sitebricks.persist.sql;

import com.google.sitebricks.persist.EntityMetadata;
import com.google.sitebricks.persist.EntityQuery;
import com.google.sitebricks.persist.EntityStore;
import com.google.sitebricks.persist.Indexed;
import com.jolbox.bonecp.BoneCP;
import org.codehaus.jackson.map.ObjectMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class SqlEntityStore extends EntityStore {

  @Inject
  private EntityMetadata metadata;

  @Inject
  private ObjectMapper objectMapper;

  private final Sql sql;

  public SqlEntityStore(BoneCP pool) {
    try {
      this.sql = new Sql(pool.getConnection());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> void save(T t) {
    EntityMetadata.EntityDescriptor descriptor = metadata.of(t.getClass());
    if (descriptor == null)
      throw new IllegalArgumentException("Object of unknown type provided. Did you remember" +
          " to register it in your persistence module via scan() or addPersistent()? [" +
          t.getClass() + "]");

    Map<String, EntityMetadata.MemberReader> fields = descriptor.fields();

//    IndexWriter writer = indexSet.current().writer;
//    Document document = new Document();
    for (Map.Entry<String, EntityMetadata.MemberReader> entry : fields.entrySet()) {
      EntityMetadata.MemberReader reader = entry.getValue();
      Collection<Annotation> annotations = reader.annotations();

      boolean indexed = false, id = false, lob = false;
      for (Annotation annotation : annotations) {
        if (Indexed.class.isInstance(annotation))
          indexed = true;
        else if (Id.class.isInstance(annotation))
          id = true;
        else if (Lob.class.isInstance(annotation))
          lob = true;
      }

      // Only index the desired fields.
      if (!indexed && !id)
        continue;

      Class<?> type = reader.type();

      Object value = reader.value(t);
    }

    try {
      // Store the entire object as JSON.
      String json = objectMapper.writeValueAsString(t);
//      document.add(new StringField(SB_TYPE, t.getClass().getName(), Field.Store.NO));
//      document.add(new StoredField(SB_OBJECT, objectMapper.writeValueAsBytes(t)));

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> T find(Class<T> type, Serializable key) {
    // Reading methods do not lock.
    EntityMetadata.EntityDescriptor descriptor = metadata.of(type);
    String idField = descriptor.idField();
    EntityMetadata.MemberReader idReader = descriptor.fields().get(idField);

    if (!idReader.type().isAssignableFrom(key.getClass()))
      throw new IllegalArgumentException("Given key is not of compatible type with @Id field of "
          + type + ". Expected: " + idReader.type() + " but found: " + key.getClass());

    Map<String, EntityQuery.FieldMatcher<?>> query =
        new HashMap<String, EntityQuery.FieldMatcher<?>>(1);
    query.put(idField, EntityQuery.FieldMatcher.is(key));

    List<T> results = execute(type, query, 0, 1);
    return results != null ? results.get(0) : null;
  }

  @Override
  public <T> void remove(Class<T> type, Serializable key) {
    EntityMetadata.EntityDescriptor descriptor = metadata.of(type);
    try {
      Map<String, EntityQuery.FieldMatcher<?>> deletionQuery =
          new HashMap<String, EntityQuery.FieldMatcher<?>>(1);
      deletionQuery.put(descriptor.idField(), EntityQuery.FieldMatcher.is(key));

//      indexSet.current().writer.deleteDocuments(queryFrom(type, deletionQuery));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected <T> void executeDelete(Class<T> type,
                                   Map<String, EntityQuery.FieldMatcher<?>> matcherMap) {
    try {
//      indexSet.current().writer.deleteDocuments(queryFrom(type, matcherMap));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected <T> List<T> execute(Class<T> type, Map<String, EntityQuery.FieldMatcher<?>> query,
                                int offset, int limit) {
    throw new AssertionError();
  }

  @Override
  public Object delegate() {
    return sql;
  }

  void complete(boolean commit) {
//    IndexWriter writer = indexSet.current().writer;
    try {
//      if (commit)
//        writer.commit();
//      else
//        writer.rollback();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      // Ensure we completely release this lock to other threads.
//      while (writeLock.isHeldByCurrentThread())
//        writeLock.unlock();
    }
  }
}
