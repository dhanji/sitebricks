package com.google.sitebricks.conversion;

import com.google.inject.ImplementedBy;

import java.lang.reflect.Type;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author John Patterson (jdpatterson@gmail.com)
 * @author JRodriguez
 */
@ImplementedBy(StandardTypeConverter.class)
public interface TypeConverter {

  /**
   * Convert an instance to the given type.
   *
   * @param source Original instance
   * @param type   The type to convert to.
   * @return A converted instance of type {@code Type}}
   */
  <T> T convert(Object source, Type type);
}
