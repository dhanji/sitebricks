package com.google.sitebricks.persist.sql;

import com.google.sitebricks.persist.EntityQuery;
import com.google.sitebricks.persist.EntityStore;
import com.jolbox.bonecp.BoneCP;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class SqlEntityStore extends EntityStore {
  private final Sql sql;

  public SqlEntityStore(BoneCP pool) {
    try {
      this.sql = new Sql(pool.getConnection());
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public <T> Serializable save(T t) {
    return null;
  }

  @Override
  public <T> T find(Class<T> type, Serializable key) {
    throw new AssertionError();
  }

  @Override public <T> List<T> all(Class<T> type) {
    throw new UnsupportedOperationException("Bulk query of all values is not supported by this datastore");
  }

  @Override
  public <T> void remove(Class<T> type, Serializable key) {
  }

  @Override
  protected <T> void executeDelete(Class<T> type,
                                   Map<String, EntityQuery.FieldMatcher<?>> matcherMap) {
  }

  @Override
  protected <T> List<T> execute(Class<T> type, Map<String, EntityQuery.FieldMatcher<?>> query,
                                int offset, int limit) {
    throw new UnsupportedOperationException("Bulk query is not supported by this datastore");
  }

  @Override
  public Object delegate() {
    return sql;
  }
}
