package com.google.sitebricks.binding;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface TypeConverter<T> {

    /**
     *
     * @param raw A string containing the raw form to coerce
     * @return Returns an instance of the converted type (from the raw string).
     */
    T convert(String raw);
}
