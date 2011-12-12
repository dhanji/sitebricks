package com.google.sitebricks.conversion;

import java.util.ArrayList;
import java.util.List;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 */
public class StringToPrimitiveConverters {
  public static List<Converter<?,?>> converters() {
    List<Converter<?,?>> converters = new ArrayList<Converter<?,?>>();
    converters.add(new ConverterAdaptor<String, Integer>() {
      public Integer to(String source) {
        return Integer.valueOf(source);
      }
    });
    converters.add(new ConverterAdaptor<String, Long>() {
      public Long to(String source) {
        return Long.valueOf(source);
      }
    });
    converters.add(new ConverterAdaptor<String, Float>() {
      public Float to(String source) {
        return Float.valueOf(source);
      }
    });
    converters.add(new ConverterAdaptor<String, Double>() {
      public Double to(String source) {
        return Double.valueOf(source);
      }
    });
    converters.add(new ConverterAdaptor<String, Byte>() {
      public Byte to(String source) {
        return Byte.valueOf(source);
      }
    });
    converters.add(new ConverterAdaptor<String, Boolean>() {
      public Boolean to(String source) {
        return Boolean.valueOf(source);
      }
    });
    converters.add(new ConverterAdaptor<String, Character>() {
      public Character to(String source) {
        return source.charAt(0);
      }
    });

    return converters;
  }
}
