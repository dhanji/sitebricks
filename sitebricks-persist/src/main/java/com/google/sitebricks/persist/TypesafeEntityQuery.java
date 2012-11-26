package com.google.sitebricks.persist;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class TypesafeEntityQuery<T> implements EntityQuery<T>, EntityQuery.Clause<T> {
  private final T topic;
  private final List<FieldMatcher<?>> matchers = new ArrayList<FieldMatcher<?>>();
  private final EntityStore store;

  public TypesafeEntityQuery(T topic, EntityStore store) {
    this.topic = topic;
    this.store = store;
  }

  @Override
  public <E> Clause<T> where(E field, FieldMatcher<E> matcher) {
    matchers.add(matcher);
    return this;
  }

  @Override
  public <E> Clause and(E field, FieldMatcher<E> matcher) {
    matchers.add(matcher);
    return this;
  }

  @Override
  public EntityQuery<T> or() {
    throw new AssertionError("not yet impl");
  }

  @Override
  public List<T> list() {
    return list(0, Integer.MAX_VALUE);
  }

  @Override
  public List<T> list(int limit) {
    return list(0, limit);
  }

  @Override
  public List<T> list(int offset, int limit) {
    // Gather into a query model.
    Map<String, FieldMatcher<?>> matcherMap = toMatcherMap();

    return store.execute(type, matcherMap, offset, limit);
  }

  private transient Class<T> type;
  private transient Map<String, FieldMatcher<?>> matcherMap;

  @SuppressWarnings("unchecked")
  private Map<String, FieldMatcher<?>> toMatcherMap() {
    if (matcherMap != null)
      return matcherMap;

    List<String> calledFields;

    try {
      Class<?> proxyClass = topic.getClass();
      type = (Class<T>) proxyClass.getMethod(TopicProxy.TYPE)
          .invoke(topic);

      // Must appear last as it freezes the object.
      calledFields = (List<String>) proxyClass.getMethod(TopicProxy.CALLED_FIELDS)
          .invoke(topic);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Unknown topic used in entity query. You MUST create the topic object" +
          " using EntityStore.topic()", e);
    } catch (InvocationTargetException e) {
      throw new IllegalStateException("Unknown topic used in entity query. You MUST create the topic object" +
          " using EntityStore.topic()", e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unknown topic used in entity query. You MUST create the topic object" +
          " using EntityStore.topic()", e);
    }

    matcherMap = new HashMap<String, FieldMatcher<?>>();
    for (int i = 0, calledFieldsSize = calledFields.size(); i < calledFieldsSize; i++) {
      String calledField = calledFields.get(i);
      matcherMap.put(calledField, matchers.get(i));
    }
    return matcherMap;
  }

  @Override
  public <T> void remove() {
    Map<String, FieldMatcher<?>> query = toMatcherMap();
    store.executeDelete(type, query);
  }
}
