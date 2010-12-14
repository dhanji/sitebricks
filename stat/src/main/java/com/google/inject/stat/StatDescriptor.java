package com.google.inject.stat;

import java.lang.reflect.Field;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class StatDescriptor {
  private final Object instance;
  private final String stat;
  private final Field field;

  StatDescriptor(Object instance, String stat, Field field) {
    this.instance = instance;
    this.stat = stat;
    this.field = field;

    if (!field.isAccessible()) {
      field.setAccessible(true);
    }
  }

  public String getStat() {
    return stat;
  }

  public Object getInstance() {
    return instance;
  }

  public Field getField() {
    return field;
  }

  public String read() {
    Object value = null;
    try {
      value = field.get(instance);
    } catch (IllegalAccessException e) {
      return "unable to read: " + e.getMessage();
    }

    return value.toString();
  }
}
