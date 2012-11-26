package com.google.sitebricks.persist;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Id;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Singleton
public class EntityMetadata {
  private final Map<Class<?>, EntityDescriptor> entityClasses;

  @Inject
  public EntityMetadata(Set<Class<?>> entityClasses) {
    this.entityClasses = new HashMap<Class<?>, EntityDescriptor>();
    for (Class<?> clazz : entityClasses) {
      this.entityClasses.put(clazz, new EntityDescriptor(clazz));
    }
  }

  public EntityDescriptor of(Class<?> clazz) {
    return entityClasses.get(clazz);
  }

  public static class EntityDescriptor {
    private final String idField;
    private final Map<String, MemberReader> fields;
    private final Class<?> entityType;

    public EntityDescriptor(Class<?> clazz) {
      String idField = null;
      this.entityType = clazz;
      Map<String, MemberReader> fields = new HashMap<String, MemberReader>();
      for (Field field : clazz.getDeclaredFields()) {
        if (Modifier.isTransient(field.getModifiers()))
          continue;

        final Field swizzleField = field;
        final Collection<Annotation> annotations = new ArrayList<Annotation>();
        for (Annotation annotation : field.getAnnotations()) {
          annotations.add(annotation);

          if (Id.class.isInstance(annotation)) {
            idField = field.getName();
          }
        }

        // Look into privates!
        if (!field.isAccessible())
          field.setAccessible(true);

        fields.put(field.getName(), new MemberReader() {
          @Override
          public Object value(Object from) {
            try {
              return swizzleField.get(from);
            } catch (IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          }

          @Override
          public Class<?> type() {
            return swizzleField.getType();
          }

          @Override
          public Collection<Annotation> annotations() {
            return annotations;
          }
        });
      }

      if (idField == null)
        throw new IllegalStateException("Entity class missing id field. At least one" +
            " serializable field must be marked with @Id but none found for: " + clazz.getName());

      this.fields = Collections.unmodifiableMap(fields);
      this.idField = idField;
    }

    public Map<String, MemberReader> fields() {
      return fields;
    }

    public String idField() {
      return idField;
    }

    public Class<?> entityType() {
      return entityType;
    }
  }

  public static interface MemberReader {
    Object value(Object from);

    Class<?> type();

    public Collection<Annotation> annotations();
  }
}
