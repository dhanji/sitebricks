package com.google.sitebricks.conversion;

import java.lang.reflect.Type;

import com.google.common.collect.Multimap;
import com.google.inject.ImplementedBy;

@ImplementedBy(StandardTypeConverter.class)
public interface ConverterRegistry {
  void register(Converter<?, ?> converter);
  Multimap<Type, Converter<?, ?>> getConvertersByTarget();
  Multimap<Type, Converter<?, ?>> getConvertersBySource();
}
