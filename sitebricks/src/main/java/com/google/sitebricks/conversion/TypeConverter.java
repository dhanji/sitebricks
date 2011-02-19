package com.google.sitebricks.conversion;

import java.lang.reflect.Type;

import com.google.inject.ImplementedBy;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author John Patterson (jdpatterson@gmail.com)
 * @author JRodriguez
 */
@ImplementedBy(StandardTypeConverter.class)
public interface TypeConverter {
    <T> T convert(Object source, Type type);
}
