package com.google.sitebricks.conversion;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.mvel2.DataConversion;

import com.google.inject.Singleton;
import com.google.sitebricks.conversion.TypeConverter;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 *
 */
@Singleton
public class MvelTypeConverter implements TypeConverter {
 	
	@Override @SuppressWarnings("unchecked")
	public <T> T convert(Object source, Type type) {
		return (T) DataConversion.convert(source, erase(type));
	}

	/**
	 * Returns the erasure of the given type.
	 * Taken from GenTyRef project
	 * TODO replace this once Sitebricks has internal generics utils
	 */
	public static Class<?> erase(Type type) {
		if (type instanceof Class<?>) {
			return (Class<?>) type;
		}
		else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		}
		else if (type instanceof TypeVariable<?>) {
			TypeVariable<?> tv = (TypeVariable<?>) type;
			if (tv.getBounds().length == 0)
				return Object.class;
			else
				return erase(tv.getBounds()[0]);
		}
		else if (type instanceof GenericArrayType) {
			GenericArrayType aType = (GenericArrayType) type;
			Class<?> componentType = erase(aType.getGenericComponentType());
			return Array.newInstance(componentType, 0).getClass();
		}
		else {
			throw new RuntimeException("not supported: " + type.getClass());
		}
	}
}
