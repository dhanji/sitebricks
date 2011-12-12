package com.google.sitebricks.conversion;

/**
 * Convert an instance from type Source to type Target and back again.
 * 
 * Returning null indicates that the conversion was not successful and another
 * converter may be given the chance to handle it. Therefore, null is not a
 * valid converted value and null will never be passed as a parameter.
 * 
 * @author John Patterson (jdpatterson@gmail.com)
 *
 * @param <S> Source Type 
 * @param <T> Target Type
 */
public interface Converter<S, T> {
    T to(S source);
    S from(T target);
}
