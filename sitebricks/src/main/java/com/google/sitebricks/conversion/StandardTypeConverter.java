package com.google.sitebricks.conversion;

import static com.google.sitebricks.conversion.generics.GenericTypeReflector.erase;
import static com.google.sitebricks.conversion.generics.GenericTypeReflector.getExactSuperType;
import static com.google.sitebricks.conversion.generics.GenericTypeReflector.getTypeParameter;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Primitives;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 */
@Singleton
public class StandardTypeConverter implements TypeConverter, ConverterRegistry {
  Multimap<Type, Converter<?, ?>> convertersBySource = ArrayListMultimap.create();
  Multimap<Type, Converter<?, ?>> convertersByTarget = ArrayListMultimap.create();

  Multimap<SourceAndTarget, Converter<?, ?>> convertersBySourceAndTarget = ArrayListMultimap.create();
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

	// special case for handling a null source 
    if (source == null) {
      return (T) nullValue(type);
    }
    
    // special case for handling empty string
    if ("".equals(source) && type != String.class && isEmptyStringNull()) {
    	return null;
    }
	  
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
      Collection<Converter<?, ?>> forwards = convertersBySourceAndTarget.get(key);
      
      // stop at the first converter that returns non-null
      for (Converter<?, ?> forward : forwards)
        if ((result = typeSafeTo(forward, source)) != null) break;
        
      if (result == null) {
        // try the reverse direction (target to source)
        Collection<Converter<?,?>> reverses = convertersBySourceAndTarget.get(key.reverse());

        // stop at the first converter that returns non-null
        for (Converter<?, ?> reverse : reverses)
          if ((result = typeSafeFrom(reverse, source)) != null) break;
      }
      
      // we have no more super classes to try
      if (sourceType == Object.class) break;
      
      // try every super type of the source
      Class<?> superClass = erase(sourceType).getSuperclass();
      sourceType = getExactSuperType(sourceType, superClass);
    }
    while (result == null);
    
    if (result == null)
      throw new IllegalStateException("Cannot convert " + source.getClass() + " to " + type);
    
    return (T) result;
  }

  protected boolean isEmptyStringNull() {
    return true;
  }

  protected Object nullValue(Type type) {
    if (type == String.class) {
	  return "";
	}
	else return null;
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
