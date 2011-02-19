package com.google.sitebricks.conversion;

import java.lang.reflect.Type;

import com.google.sitebricks.conversion.TypeConverter;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class TestTypeConverter implements TypeConverter {
    @SuppressWarnings("unchecked")
	public <T> T convert(Object raw, Type type) {
        return (T) this;
    }
}
