package com.google.sitebricks.conversion;

import com.google.sitebricks.SitebricksModule;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 */
public class StringToPrimitiveConverters {
  public static void register(SitebricksModule module) {
    module.converter(new ConverterAdaptor<String, Integer>() {
      public Integer to(String source) {
        return Integer.valueOf(source);
      }
    });
    module.converter(new ConverterAdaptor<String, Long>() {
      public Long to(String source) {
        return Long.valueOf(source);
      }
    });
    module.converter(new ConverterAdaptor<String, Float>() {
      public Float to(String source) {
        return Float.valueOf(source);
      }
    });
    module.converter(new ConverterAdaptor<String, Double>() {
      public Double to(String source) {
        return Double.valueOf(source);
      }
    });
    module.converter(new ConverterAdaptor<String, Byte>() {
      public Byte to(String source) {
        return Byte.valueOf(source);
      }
    });
    module.converter(new ConverterAdaptor<String, Boolean>() {
      public Boolean to(String source) {
        return Boolean.valueOf(source);
      }
    });
    module.converter(new ConverterAdaptor<String, Character>() {
      public Character to(String source) {
        return source.charAt(0);
      }
    });
  }
}
