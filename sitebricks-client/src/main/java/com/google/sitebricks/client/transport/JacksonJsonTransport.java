package com.google.sitebricks.client.transport;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.primitives.Primitives;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.sitebricks.conversion.Converter;
import com.google.sitebricks.conversion.ConverterRegistry;
import com.google.sitebricks.conversion.StandardTypeConverter;
import com.google.sitebricks.conversion.generics.Generics;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.CustomDeserializerFactory;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author John Patterson (jdpatterson@gmail.com)
 * @author JRodriguez
 */
@Singleton
public class JacksonJsonTransport extends Json {

  private final ObjectMapper objectMapper;
  private Collection<Class<?>> exceptions = Sets.newHashSet();
  
  @Inject
  public JacksonJsonTransport(ConverterRegistry registry, Provider<ObjectMapper> objectMapperProvider) {
    this.objectMapper = objectMapperProvider.get();
    CustomDeserializerFactory deserializerFactory = new CustomDeserializerFactory();
    
    // leave these for Jackson to handle
    exceptions.add(String.class);
    exceptions.add(Object.class);
    exceptions.addAll(Primitives.allWrapperTypes());

    // 
    Multimap<Type, ConverterDirection> typeToConverterDirection = ArrayListMultimap.create();
    addConverterDirections(registry, true, typeToConverterDirection);
    addConverterDirections(registry, false, typeToConverterDirection);
    createJacksonDeserializers(deserializerFactory, typeToConverterDirection);
    
    objectMapper.setDeserializerProvider(new StdDeserializerProvider(deserializerFactory));
  }
  
  public ObjectMapper getObjectMapper() {
	return objectMapper;
  }
  
  // keep track of which direction we want to use
  private static class ConverterDirection
  {
    Converter<?, ?> converter;
    boolean forward;
  }

  private void addConverterDirections(ConverterRegistry registry, boolean forward, Multimap<Type, ConverterDirection> typeToConverterDirections) {
    Multimap<Type, Converter<?, ?>> typeToConverters = forward ? registry.getConvertersByTarget() : registry.getConvertersBySource();
    Set<Type> types = typeToConverters.keySet();
    for (Type type : types) {
      if (exceptions.contains(type)) continue;
      Collection<Converter<?, ?>> converters = typeToConverters.get(type);
      for (Converter<?, ?> converter : converters) {
        ConverterDirection converterDirection = new ConverterDirection();
        converterDirection.converter = converter;
        converterDirection.forward = forward;
        typeToConverterDirections.put(type, converterDirection);
      }
    }
  }
  
  private void createJacksonDeserializers(CustomDeserializerFactory deserializerFactory, Multimap<Type, ConverterDirection> typeToConverterDirections)
  {
    Set<Type> targetTypes = typeToConverterDirections.keySet();
    for (Type targetType : targetTypes) {
      Collection<ConverterDirection> converterDirections = typeToConverterDirections.get(targetType);
      Class<?> targetClass = Generics.erase(targetType);
      ConvertersDeserializer jds = new ConvertersDeserializer(converterDirections);
      typesafeAddMapping(targetClass, jds, deserializerFactory);
    }
  }

  @SuppressWarnings("unchecked")
  private <T> void typesafeAddMapping(Class<?> type, JsonDeserializer<T> deserializer,
      CustomDeserializerFactory factory) {
    factory.addSpecificMapping((Class<T>) type, deserializer);
  }

  public <T> T in(InputStream in, Class<T> type) throws IOException {
    return objectMapper.readValue(in, type);
  }

  public <T> void out(OutputStream out, Class<T> type, T data) {
    try {
      objectMapper.writeValue(out, data);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public class ConvertersDeserializer extends JsonDeserializer<Object> {

    private final Collection<ConverterDirection> converterDirections;

    public ConvertersDeserializer(Collection<ConverterDirection> converterDirections) {
      this.converterDirections = converterDirections;
    }

    public Object deserialize(JsonParser jp, DeserializationContext ctxt) 
        throws IOException, JsonProcessingException {

      Object source = getSourceObject(jp, ctxt);
      
      for (ConverterDirection converterDirection : converterDirections) {
        
        Type sourceType = converterDirection.forward ? 
            StandardTypeConverter.sourceType(converterDirection.converter) : 
            StandardTypeConverter.targetType(converterDirection.converter);
            
        // assume that Jackson only gives us non-generic types
        Class<?> converterSourceClass = Generics.erase(sourceType);
        
        if (converterSourceClass.isAssignableFrom(source.getClass())) {
          return converterDirection.forward ? 
              StandardTypeConverter.typeSafeTo(converterDirection.converter, source) : 
              StandardTypeConverter.typeSafeFrom(converterDirection.converter, source); 
        }
      }

      throw new IllegalStateException("Cannot convert from " + source);
    }

    private Object getSourceObject(JsonParser jp, DeserializationContext ctxt) throws JsonParseException, IOException {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NUMBER_INT) {
          return jp.getLongValue();
        }
        else if (t == JsonToken.VALUE_NUMBER_FLOAT) {
          return jp.getDoubleValue();
        }
        else if (t == JsonToken.VALUE_TRUE) {
          return Boolean.TRUE;
        }
        else if (t == JsonToken.VALUE_FALSE) {
            return Boolean.FALSE;
        }
        else if (t == JsonToken.VALUE_STRING) {
          return jp.getText();
        }
        else throw new IllegalStateException();
    }
  }
}
