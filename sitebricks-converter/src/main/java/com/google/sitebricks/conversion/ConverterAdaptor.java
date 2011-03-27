package com.google.sitebricks.conversion;

/**
 * @author John Patterson (jdpatterson@gmail.com)
 */
abstract class ConverterAdaptor<S, T> implements Converter<S, T> {
  @Override
  public S from(T target) {
    return null;
  }
}