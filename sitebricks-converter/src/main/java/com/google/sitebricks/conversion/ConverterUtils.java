package com.google.sitebricks.conversion;

import com.google.inject.multibindings.Multibinder;

public class ConverterUtils {
  //
  // I need to pass in the Multibinder because order in which bindings are made affects the
  // outcome of the tests. So I would prefer to just hand back the Multibinder creating it from
  // scratch but we can't right now. jvz. (yes, this class won't be around long)
  //
  public static Multibinder<Converter> createConverterMultibinder(Multibinder<Converter> converters) {
      
    // register the default converters after user converters
    converters.addBinding().to(ObjectToStringConverter.class);
    
    // allow single request parameters to be converted to List<String> 
    converters.addBinding().to(SingletonListConverter.class);
    
    for( Converter<?,?> converter : StringToPrimitiveConverters.converters() )
    {
      converters.addBinding().toInstance(converter);
    }

    for( Converter<?,?> converter : NumberConverters.converters() )
    {
      converters.addBinding().toInstance(converter);
    }
    
    for( Class<? extends Converter<?, ?>> converterClass : DateConverters.converters())
    {
      converters.addBinding().to(converterClass);
    }   

    return converters;
  
  }

}
