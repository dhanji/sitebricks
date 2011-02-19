package com.google.sitebricks.conversion;

import static com.google.sitebricks.conversion.generics.GenericTypeReflector.erase;
import static com.google.sitebricks.conversion.generics.GenericTypeReflector.getExactSuperType;
import static com.google.sitebricks.conversion.generics.GenericTypeReflector.getTypeParameter;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Primitives;
import com.google.inject.Inject;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 *
 */
public class StandardTypeConverter implements TypeConverter, ConverterRegistry {
  Multimap<Type, Converter<?, ?>> convertersBySource = ArrayListMultimap.create();
  Multimap<Type, Converter<?, ?>> convertersByTarget = ArrayListMultimap.create();

  Map<SourceAndTarget, Converter<?, ?>> convertersBySourceAndTarget = Maps.newHashMap();
  private static final TypeVariable<? extends Class<?>> sourceTypeParameter = Converter.class.getTypeParameters()[0];
  private static final TypeVariable<? extends Class<?>> targetTypeParameter = Converter.class.getTypeParameters()[1];

  public StandardTypeConverter() {
  }

  @Inject
  public StandardTypeConverter(@SuppressWarnings("rawtypes") Set<Converter> converters) {
    for (Converter<?, ?> converter : converters) {
      register(converter);
    }
  }

  @Override
  public void register(Converter<?, ?> converter) {
    // get the source and target types
    Type sourceType = sourceType(converter);
    Type targetType = targetType(converter);
    convertersBySource.put(sourceType, converter);
    convertersByTarget.put(targetType, converter);
    convertersBySourceAndTarget.put(new SourceAndTarget(sourceType, targetType), converter);
  }

  public static Type targetType(Converter<?, ?> converter) {
    return getTypeParameter(converter.getClass(), targetTypeParameter);
  }

  public static Type sourceType(Converter<?, ?> converter) {
    return getTypeParameter(converter.getClass(), sourceTypeParameter);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T convert(final Object source, Type type) {
    
    // use primitive wrapper types
    if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
        type = Primitives.wrap((Class<?>) type);
    }
    
    if (source.getClass() == type) {
      return (T) source;
    }
    
    if (source.getClass() == Object.class || type == Object.class) {
      throw new IllegalArgumentException("Object is invalid converter type");
    }
    
    Type sourceType = source.getClass();
    
    // look for converters for exact types or super types  
    Object result = null;
    do {
      
      // first try to find a converter in the forward direction 
      SourceAndTarget key = new SourceAndTarget(sourceType, type);
      Converter<?, ?> forward = convertersBySourceAndTarget.get(key);
      if (forward != null) {
        result = typeSafeTo(forward, source);
      } else {
        // now try the reverse direction (target to source)
        Converter<?, ?> reverse = convertersBySourceAndTarget.get(key.reverse());
        if (reverse != null) {
          result = typeSafeFrom(reverse, source);
        }
      }
      
      if (sourceType == Object.class) break;
      
      // try every permutation of source and target type
      Class<?> superClass = erase(sourceType).getSuperclass();
      sourceType = getExactSuperType(sourceType, superClass);
    }
    while (result == null);
    
    if (result == null)
      throw new IllegalStateException("Cannot convert " + source.getClass() + " to " + type);
    
    return (T) result;
  }

  @SuppressWarnings("unchecked")
  public static <T, S> T typeSafeTo(Converter<?, ?> converter, S source) {
    return ((Converter<S, T>) converter).to(source);
  }
  
  @SuppressWarnings("unchecked")
  public static <T, S> S typeSafeFrom(Converter<?, ?> converter, T source) {
    return ((Converter<S, T>) converter).from(source);
  }
  
  @Override
  public Multimap<Type, Converter<?, ?>> getConvertersBySource() {
    return convertersBySource;
  }
  
  @Override
  public Multimap<Type, Converter<?, ?>> getConvertersByTarget() {
    return convertersByTarget;
  }

  private static final class SourceAndTarget {
    private Type source;
    private Type target;

    public SourceAndTarget(Type source, Type target) {
      this.source = source;
      this.target = target;
    }

    public SourceAndTarget reverse() {
      return new SourceAndTarget(target, source);
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((source == null) ? 0 : source.hashCode());
      result = prime * result + ((target == null) ? 0 : target.hashCode());
      return result;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      SourceAndTarget other = (SourceAndTarget) obj;
      if (source == null) {
        if (other.source != null)
          return false;
      } else if (!source.equals(other.source))
        return false;
      if (target == null) {
        if (other.target != null)
          return false;
      } else if (!target.equals(other.target))
        return false;
      return true;
    }
  }
}
