package com.google.sitebricks.persist;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A great gigantic hack that allows us to do completely type-safe queries.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class TopicProxy implements MethodInterceptor {
  public static final String CALLED_FIELDS = "__$get$TopicCalledFields";
  public static final String TYPE = "__$get$TopicType";
  private final EntityMetadata.EntityDescriptor descriptor;
  private final List<String> calledFields = new ArrayList<String>();
  private boolean discard;

  public TopicProxy(EntityMetadata.EntityDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  public static interface HasCalledFields {
    List<String> __$get$TopicCalledFields();
    Class<?> __$get$TopicType();
  }

  @Override
  public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
    if (discard)
      throw new IllegalStateException("A topic object cannot be reused. Please create a new one" +
          " by calling EntityStore.topic()");

    String name = method.getName();
    if (method.getParameterTypes().length != 0)
      throw new IllegalArgumentException("Please use only property getters to build up a natural type-safe query.");

    if (name.equals(CALLED_FIELDS)) {
      discard = true;
      return calledFields;
    } else if (name.equals(TYPE))
      return descriptor.entityType();

    if (name.startsWith("get") && name.length() > 3)
      name = Character.toLowerCase(name.charAt(3)) + name.substring(4);

    if (!descriptor.fields().containsKey(name))
      throw new IllegalArgumentException("Field is not a persistent member of the topic entity: "
          + descriptor.entityType().getName() + "#" + name);

    calledFields.add(name);
    return null;
  }
}
