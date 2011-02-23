package com.google.sitebricks.conversion;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

import org.mvel2.ConversionHandler;
import org.mvel2.DataConversion;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import com.google.inject.Inject;
import com.google.sitebricks.conversion.generics.GenericTypeReflector;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 */
public class MvelConversionHandlers
{
	private static Collection<Class<?>> exceptions = Sets.newHashSet();
	static
	{
		// leave these types for "native" handling by mvel
		exceptions.add(String.class);
		exceptions.add(Object.class);
		exceptions.addAll(Primitives.allWrapperTypes());
	}

	@Inject
	public static void register(ConverterRegistry registry)
	{
		Multimap<Type, ConverterDirection> typeToConverterDirections = ArrayListMultimap.create();
		addConverterDirections(registry, true, typeToConverterDirections);
		addConverterDirections(registry, false, typeToConverterDirections);
		createConversionHandlers(typeToConverterDirections);
	}

	private static void createConversionHandlers(Multimap<Type, ConverterDirection> typeToConverterDirections)
	{
		Set<Type> targetTypes = typeToConverterDirections.keySet();
		for (Type targetType : targetTypes)
		{
			Collection<ConverterDirection> converterDirections = typeToConverterDirections.get(targetType);
			Class<?> targetClass = GenericTypeReflector.erase(targetType);
			SitebricksConversionHandler handler = new SitebricksConversionHandler(converterDirections);
			DataConversion.addConversionHandler(targetClass, handler);
		}
	}

	private static void addConverterDirections(ConverterRegistry registry, boolean forward,
			Multimap<Type, ConverterDirection> typeToConverterDirections)
	{
		Multimap<Type, Converter<?, ?>> typeToConverters = forward ? 
				registry.getConvertersByTarget() : 
				registry.getConvertersBySource();
				
		Set<Type> types = typeToConverters.keySet();
		for (Type type : types)
		{
			if (exceptions.contains(type)) continue;
			
			Collection<Converter<?, ?>> converters = typeToConverters.get(type);
			for (Converter<?, ?> converter : converters)
			{
				ConverterDirection converterDirection = new ConverterDirection();
				converterDirection.converter = converter;
				converterDirection.forward = forward;
				typeToConverterDirections.put(type, converterDirection);
			}
		}
	}

	private static class SitebricksConversionHandler implements ConversionHandler
	{
		private Collection<ConverterDirection> converterDirections;

		public SitebricksConversionHandler(Collection<ConverterDirection> converterDirections)
		{
			this.converterDirections = converterDirections;
		}

		@Override
		public Object convertFrom(Object source)
		{
			ConverterDirection converterDirection = getConversionDirection(source.getClass());

			if (converterDirection != null)
			{
				return converterDirection.forward ? 
					StandardTypeConverter.typeSafeTo(converterDirection.converter, source) : 
					StandardTypeConverter.typeSafeFrom(converterDirection.converter, source);
			}
			else
			{
				throw new IllegalStateException("Cannot convert from " + source);
			}
		}

		private ConverterDirection getConversionDirection(Class<?> clazz)
		{
			for (ConverterDirection converterDirection : converterDirections)
			{
				Type sourceType = converterDirection.forward ? 
					StandardTypeConverter.sourceType(converterDirection.converter) : 
					StandardTypeConverter.targetType(converterDirection.converter);

				// assume that Jackson only gives us non-generic types
				Class<?> converterSourceClass = GenericTypeReflector.erase(sourceType);

				if (converterSourceClass.isAssignableFrom(clazz))
				{
					return converterDirection;
				}
			}
			return null;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public boolean canConvertFrom(Class cls)
		{
			return getConversionDirection(cls) != null;
		}
	}

	// keep track of which direction we want to use
	private static class ConverterDirection
	{
		Converter<?, ?> converter;
		boolean forward;
	}
}
