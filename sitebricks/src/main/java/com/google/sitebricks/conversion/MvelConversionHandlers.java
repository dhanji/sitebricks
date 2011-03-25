package com.google.sitebricks.conversion;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;

import com.google.common.primitives.Primitives;
import com.google.inject.Inject;
import com.google.sitebricks.conversion.generics.Generics;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 */
public class MvelConversionHandlers {

  private TypeConverter delegate;
  private ConverterRegistry registry;

  @Inject
  public void prepare(ConverterRegistry registry, TypeConverter delegate) {
    this.registry = registry;
    this.delegate = delegate;

    Collection<Converter<?, ?>> converters = registry.getConvertersBySource().values();
    for (Converter<?, ?> converter : converters) {
      ParameterizedType converterType = (ParameterizedType) Generics.getExactSuperType(
          converter.getClass(), Converter.class);

      Type[] converterParameters = converterType.getActualTypeArguments();
      registerMvelHandler(converterParameters[0]);
      registerMvelHandler(converterParameters[1]);
    }
  }

  private void registerMvelHandler(Type targetType) {
    Class<?> targetClass = Generics.erase(targetType);
    SitebricksConversionHandler targetHandler = new SitebricksConversionHandler(targetType);
    DataConversion.addConversionHandler(targetClass, targetHandler);
    if (Primitives.isWrapperType(targetClass)) {
      DataConversion.addConversionHandler(Primitives.unwrap(targetClass), targetHandler);
    }
  }

  private class SitebricksConversionHandler implements ConversionHandler {
    private final Type targetType;

    public SitebricksConversionHandler(Type targetType) {
      this.targetType = targetType;
    }

    @Override
    public Object convertFrom(Object in) {
      return delegate.convert(in, targetType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean canConvertFrom(@SuppressWarnings("rawtypes") Class cls) {
      if (cls == targetType)
        return true;
      
      // check that there is a converter registered for this source type
      return registry.converter(Primitives.wrap(cls), targetType) != null;
    }
  }
}
