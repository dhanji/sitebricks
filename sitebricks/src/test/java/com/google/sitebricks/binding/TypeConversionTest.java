package com.google.sitebricks.binding;

import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class TypeConversionTest {
    @Test
    public final void coerceUserType() {
        final TypeConverter<TestTypeConverter> test = new TestTypeConverter();

        assert test.equals(test.convert("hello"));
    }
    
}
