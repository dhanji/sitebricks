package com.google.sitebricks.conversion;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 */
public class NumberConverters {
  public static List<Converter<?,?>> converters() {
    List<Converter<?,?>> converters = new ArrayList<Converter<?,?>>(); 
      
    converters.add(new ConverterAdaptor<Number, Integer>() {
      public Integer to(Number source) {
        return Integer.valueOf(source.intValue());
      }
    });
    converters.add(new ConverterAdaptor<Number, Long>() {
      public Long to(Number source) {
        return Long.valueOf(source.longValue());
      }
    });
    converters.add(new ConverterAdaptor<Number, Float>() {
      public Float to(Number source) {
        return Float.valueOf(source.floatValue());
      }
    });
    converters.add(new ConverterAdaptor<Number, Double>() {
      public Double to(Number source) {
        return Double.valueOf(source.doubleValue());
      }
    });
    converters.add(new ConverterAdaptor<Number, Short>() {
      public Short to(Number source) {
        return Short.valueOf(source.shortValue());
      }
    });
    converters.add(new ConverterAdaptor<Number, BigInteger>() {
      public BigInteger to(Number source) {
        return BigInteger.valueOf(source.longValue());
      }
    });
    converters.add(new ConverterAdaptor<Number, BigDecimal>() {
      public BigDecimal to(Number source) {
        return BigDecimal.valueOf(source.doubleValue());
      }
    });
    
    return converters;
  }
}
