package com.google.sitebricks.options;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.sitebricks.conversion.MvelTypeConverter;
import com.google.sitebricks.conversion.TypeConverter;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Hacky wrapper of MvelTypeConverter to support sets.
 *
 * @author jochen@pedesis.org (Jochen Bekmann)
 */
public class OptionTypeConverter implements TypeConverter {
  @Inject
  MvelTypeConverter converter;

  @Override
  @SuppressWarnings("unchecked")
  public <T> T convert(Object source, Type type) {
    if (Set.class == type && String.class == source.getClass()) {
      Set<String> set = Sets.newHashSet();
      for (String s : ((String) source).split(","))
        set.add(s.trim());
      return (T) set;
    }
    return converter.convert(source, type);
  }
}
