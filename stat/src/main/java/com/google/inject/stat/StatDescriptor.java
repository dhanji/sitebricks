package com.google.inject.stat;

import java.lang.reflect.Field;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public final class StatDescriptor {
  private final Object target;
  private final String name;
  private final String description;
  private final Field field;

  public StatDescriptor(
      Object target, String name, String description, Field field) {
    this.target = target;
    this.name = name;
    this.description = description;
    this.field = field;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Object getTarget() {
    return target;
  }

  public Field getField() {
    return field;
  }
}
