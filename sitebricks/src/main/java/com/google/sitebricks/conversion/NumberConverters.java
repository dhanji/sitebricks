package com.google.sitebricks.conversion;

import java.math.BigDecimal;
import java.math.BigInteger;

import com.google.sitebricks.SitebricksModule;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 */
public class NumberConverters {
  public static void register(SitebricksModule module) {
    module.converter(new ConverterAdaptor<Number, Integer>() {
      public Integer to(Number source) {
        return Integer.valueOf(source.intValue());
      }
    });
    module.converter(new ConverterAdaptor<Number, Long>() {
      public Long to(Number source) {
        return Long.valueOf(source.longValue());
      }
    });
    module.converter(new ConverterAdaptor<Number, Float>() {
      public Float to(Number source) {
        return Float.valueOf(source.floatValue());
      }
    });
    module.converter(new ConverterAdaptor<Number, Double>() {
      public Double to(Number source) {
        return Double.valueOf(source.doubleValue());
      }
    });
    module.converter(new ConverterAdaptor<Number, Short>() {
      public Short to(Number source) {
        return Short.valueOf(source.shortValue());
      }
    });
    module.converter(new ConverterAdaptor<Number, BigInteger>() {
      public BigInteger to(Number source) {
        return BigInteger.valueOf(source.longValue());
      }
    });
    module.converter(new ConverterAdaptor<Number, BigDecimal>() {
      public BigDecimal to(Number source) {
        return BigDecimal.valueOf(source.doubleValue());
      }
    });
  }
}
