package com.google.sitebricks.persist.disk;

import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.BytesRef;
import org.codehaus.jackson.map.ObjectMapper;
import com.google.sitebricks.persist.EntityMetadata;
import com.google.sitebricks.persist.EntityQuery;
import com.google.sitebricks.persist.EntityStore;
import com.google.sitebricks.persist.Indexed;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
class DiskEntityStore extends EntityStore {
  public static final String SB_TYPE = "__sb$type$";
  public static final String SB_OBJECT = "__sb$object$";

  private final ReentrantLock writeLock = new ReentrantLock(true);

  private IndexSet indexSet;

  @Inject
  private EntityMetadata metadata;

  @Inject
  private ObjectMapper objectMapper;

  public void init(IndexSet indexSet) {
    this.indexSet = indexSet;
  }

  void lock() {
    // Ensure we only lock once from this thread.
    if (!writeLock.isHeldByCurrentThread())
      writeLock.lock();
  }

  @Override
  public <T> Serializable save(T t) {
    EntityMetadata.EntityDescriptor descriptor = metadata.of(t.getClass());
    if (descriptor == null)
      throw new IllegalArgumentException("Object of unknown type provided. Did you remember" +
          " to register it in your persistence module via scan() or addPersistent()? [" +
          t.getClass() + "]");

    lock();
    Map<String, EntityMetadata.MemberReader> fields = descriptor.fields();

    IndexWriter writer = indexSet.current().writer;
    Serializable idValue = null;
    Document document = new Document();
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
      IndexableField field;

      Object value = reader.value(t);
      if (id) {
        if (value == null)
          throw new IllegalArgumentException("You must provide an id (disk store does not autopopulate id");
        else
          idValue = (Serializable) value;
      }

      if (type == int.class || type == Integer.class)
        field = new IntField(entry.getKey(), (Integer) value, Field.Store.NO);
      else if (type == long.class || type == Long.class)
        field = new LongField(entry.getKey(), (Long) value, Field.Store.NO);
      else if (type == double.class || type == Double.class)
        field = new DoubleField(entry.getKey(), (Double) value, Field.Store.NO);
      else if (type == float.class || type == Float.class)
        field = new FloatField(entry.getKey(), (Float) value, Field.Store.NO);
      else {
        if (lob)
          field = new TextField(entry.getKey(), (String) value, Field.Store.NO);
        else {
          if (value != null)
            value = value.toString();

          field = new StringField(entry.getKey(), (String) value, Field.Store.NO);
        }
      }

      document.add(field);
    }

    try {
      // Store the entire object as JSON.
      document.add(new StringField(SB_TYPE, t.getClass().getName(), Field.Store.NO));
      document.add(new StoredField(SB_OBJECT, objectMapper.writeValueAsBytes(t)));

      writer.addDocument(document);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return idValue;
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

      indexSet.current().writer.deleteDocuments(queryFrom(type, deletionQuery));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected <T> void executeDelete(Class<T> type,
                                   Map<String, EntityQuery.FieldMatcher<?>> matcherMap) {
    try {
      indexSet.current().writer.deleteDocuments(queryFrom(type, matcherMap));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected <T> List<T> execute(Class<T> type, Map<String, EntityQuery.FieldMatcher<?>> query,
                                int offset, int limit) {
    BooleanQuery booleanQuery = queryFrom(type, query);
    IndexSearcher searcher = indexSet.current().searcher.get();
    try {
    TopDocs results = searcher.search(booleanQuery, limit);
    if (results.scoreDocs.length == 0)
      return null;

      List<T> resultList = new ArrayList<T>(results.scoreDocs.length - offset);
      for (int i = offset; i < results.scoreDocs.length; i++) {
         Document document = searcher.doc(results.scoreDocs[i].doc);
         resultList.add(objectMapper.readValue(document.getField(SB_OBJECT).binaryValue().bytes, type));
      }

      return resultList;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void addNumericConstraint(Number low, Number high, String field, BooleanQuery toQuery,
                                    boolean includeLow, boolean includeHigh) {
    if (high == null)
      high = low;

    // Account for various numeric types.
    if (low instanceof Integer)
      toQuery.add(NumericRangeQuery.newIntRange(field, (Integer) low, (Integer) high, includeLow, includeHigh),
          BooleanClause.Occur.MUST);
    else if (low instanceof Double)
      toQuery.add(NumericRangeQuery.newDoubleRange(field, (Double) low, (Double) high, includeLow, includeHigh),
          BooleanClause.Occur.MUST);
    else
      toQuery.add(NumericRangeQuery.newLongRange(field, (Long) low, (Long) high, includeLow, includeHigh),
          BooleanClause.Occur.MUST);
  }

  private <T> BooleanQuery queryFrom(Class<T> type, Map<String, EntityQuery.FieldMatcher<?>> query) {
    BooleanQuery booleanQuery = new BooleanQuery();
    booleanQuery.add(new TermQuery(new Term(SB_TYPE, QueryParser.escape(type.getName()))),
        BooleanClause.Occur.MUST);

    for (Map.Entry<String, EntityQuery.FieldMatcher<?>> matchers : query.entrySet()) {
      String field = matchers.getKey();
      EntityQuery.FieldMatcher<?> value = matchers.getValue();

      if (value.low instanceof Number) {
        switch (value.kind) {
          case IS:
            addNumericConstraint((Number)value.low, null, field, booleanQuery, true, true);
            break;
          case NOT:
            addNumericConstraint((Number)value.low, null, field, booleanQuery, false, false);
            break;
          case BELOW:
            addNumericConstraint(Integer.MIN_VALUE, (Number)value.low, field, booleanQuery, false, false);
            break;
          case ABOVE:
            addNumericConstraint((Number)value.low, Integer.MAX_VALUE, field, booleanQuery, false, false);
            break;
          case BELOW_INCLUDING:
            addNumericConstraint(Integer.MIN_VALUE, (Number)value.low, field, booleanQuery, false, true);
            break;
          case ABOVE_INCLUDING:
            addNumericConstraint((Number)value.low, Integer.MAX_VALUE, field, booleanQuery, true, false);
            break;
          case BETWEEN:
            addNumericConstraint((Number)value.low, (Number)value.high, field, booleanQuery, false, false);
            break;
          default:
            throw new UnsupportedOperationException("Query type not supported for numbers in this datastore: "
                + value.kind);
        }
      } else {
        switch (value.kind) {
          case IS:
            booleanQuery.add(new TermQuery(new Term(field, value.low.toString())),
                BooleanClause.Occur.MUST);
            break;
          case NOT:
            booleanQuery.add(new TermQuery(new Term(field, value.low.toString())),
                BooleanClause.Occur.MUST_NOT);
            break;
          case LIKE:
            // Prefix search.
            booleanQuery.add(new PrefixQuery(new Term(field, value.low.toString())),
                BooleanClause.Occur.MUST);
            break;
          case SIMILAR_TO:
            booleanQuery.add(new FuzzyQuery(new Term(field, value.low.toString())),
                BooleanClause.Occur.MUST);
            break;
          case BETWEEN:
            booleanQuery.add(new TermRangeQuery(field, new BytesRef(value.low.toString()),
                new BytesRef(value.high.toString()), false, false), BooleanClause.Occur.MUST);
            break;
          default:
            throw new UnsupportedOperationException("Query type not supported for strings in this datastore: "
                + value.kind);
        }
      }
    }
    return booleanQuery;
  }

  @Override
  public Object delegate() {
    return indexSet;
  }

  void complete(boolean commit) {
    IndexWriter writer = indexSet.current().writer;
    try {
      if (commit)
        writer.commit();
      else
        writer.rollback();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      // Ensure we completely release this lock to other threads.
      while (writeLock.isHeldByCurrentThread())
        writeLock.unlock();
    }
  }
}
