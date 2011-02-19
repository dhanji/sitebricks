package com.google.sitebricks.client.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.deser.CustomDeserializerFactory;
import org.codehaus.jackson.map.deser.StdDeserializerProvider;
import org.testng.v6.Sets;

import com.google.common.collect.Multimap;
import com.google.common.primitives.Primitives;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.sitebricks.conversion.Converter;
import com.google.sitebricks.conversion.ConverterRegistry;
import com.google.sitebricks.conversion.StandardTypeConverter;
import com.google.sitebricks.conversion.generics.GenericTypeReflector;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 * @author John Patterson (jdpatterson@gmail.com)
 * @author JRodriguez
 */
@Singleton
class JacksonJsonTransport extends Json {

  private final ObjectMapper objectMapper;
  private Collection<Class<?>> exceptions = Sets.newHashSet();

  @Inject
  public JacksonJsonTransport(ConverterRegistry registry) {
    this.objectMapper = new ObjectMapper();
    CustomDeserializerFactory deserializerFactory = new CustomDeserializerFactory();
    
    // leave these for Jackson to handle
    exceptions.add(String.class);
    exceptions.addAll(Primitives.allWrapperTypes());

    addConverters(registry, deserializerFactory, true);
    addConverters(registry, deserializerFactory, false);

    objectMapper.setDeserializerProvider(new StdDeserializerProvider(deserializerFactory));
  }

  // TODO skip primitive wrapper converters?
  private void addConverters(ConverterRegistry registry, CustomDeserializerFactory deserializerFactory, boolean forward) {
    Multimap<Type, Converter<?, ?>> typeToConverters = forward ? registry.getConvertersByTarget() : registry.getConvertersBySource();
    Set<Type> types = typeToConverters.keySet();
    for (Type type : types) {
      if (exceptions.contains(type)) continue;
      Collection<Converter<?, ?>> converters = typeToConverters.get(type);
      Class<?> erased = GenericTypeReflector.erase(type);
      ConvertersDeserializer jds = new ConvertersDeserializer(erased, converters, forward);
      typesafeAddMapping(erased, jds, deserializerFactory);
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
    private final Class<?> targetType;
    private final Collection<Converter<?, ?>> converters;
    private final boolean forward;

    public ConvertersDeserializer(Class<?> targetType, Collection<Converter<?, ?>> converters, boolean forward) {
      this.targetType = targetType;
      this.converters = converters;
      this.forward = forward;
    }

    public Object deserialize(JsonParser jp, DeserializationContext ctxt) 
        throws IOException, JsonProcessingException {

      Object source = getSourceObject(jp, ctxt);
      
      for (Converter<?, ?> converter : converters) {
        Type sourceType = forward ? 
            StandardTypeConverter.sourceType(converter) : 
            StandardTypeConverter.targetType(converter);
            
        // assume that Jackson only gives us non-generic types
        Class<?> converterSourceClass = GenericTypeReflector.erase(sourceType);
        
        if (converterSourceClass.isAssignableFrom(source.getClass())) {
          return forward ? 
              StandardTypeConverter.typeSafeTo(converter, source) : 
              StandardTypeConverter.typeSafeFrom(converter, source); 
        }
      }

      throw ctxt.mappingException(targetType);
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
