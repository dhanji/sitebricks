package com.google.sitebricks.conversion;

import java.lang.reflect.Type;

/**
 * Noop implementation that returns the source unaltered.
 * 
 * @author John Patterson (jdpatterson@gmail.com)
 *
 */
public class DummyTypeConverter implements TypeConverter
{
	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(Object source, Type type)
	{
		return (T) source;
	}
}
