package com.google.sitebricks.conversion;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;
import com.google.sitebricks.conversion.DateConverters.DateStringConverter;

/**
 * @author JRodriguez
 * @author John Patterson (jdpatterson@gmail.com)
 */
public class StandardTypeConverterTest {
  
  private TypeConverter converter;
  
  // a very weird date format
  private String format = "ddd MM yy-EE a";

  @BeforeTest
  public void setup() {
    

    Injector injector = Guice.createInjector(new Module() {
      @Override
      public void configure(Binder binder) {
        //
        // If the DateStringConverter is not added here first then the tests fail...
        // There needs to be some way to override converters in a sane way.
        //
        Multibinder<Converter> converters = Multibinder.newSetBinder(binder, Converter.class);
        converters.addBinding().toInstance(new DateStringConverter(format));
        ConverterUtils.createConverterMultibinder(converters);        
      }      
    });
    
    converter = injector.getInstance(StandardTypeConverter.class);
  }

  @Test
  public void stringToPrimitive() {
    Integer answer = converter.convert("42", Integer.class);
    assert answer == 42;
  }

  @Test
  public void numbers() {
    BigDecimal answer = converter.convert(42, BigDecimal.class);
    assert answer.intValue() == 42;
  }
  
  @Test
  public void dateToString() {
	  SimpleDateFormat sdf = new SimpleDateFormat (format);
	  Date date = new Date();
    String answer = converter.convert(date, String.class);
    assert answer.equals(sdf.format(date));
  }
  
  @Test
  public void stringToDate() {
	  SimpleDateFormat sdf = new SimpleDateFormat(format);
	  Date original = new Date();
	  String expected = sdf.format(original);	
    Date converted = converter.convert(expected, Date.class);
    String actual = sdf.format(converted);
    assert actual.equals(expected);
  }

  @Test
  public void calendarToString() {
	  SimpleDateFormat sdf = new SimpleDateFormat(format);
	  Calendar calendar = Calendar.getInstance();	
    String answer = converter.convert(calendar, String.class);
    String expected = sdf.format(calendar.getTime());
    System.out.println( ">> " + answer );
    System.out.println( ">> " + expected );
    assert answer.equals(expected);
  }

  @Test
  public void stringToCalendar() {
	  SimpleDateFormat sdf = new SimpleDateFormat(format);
	  Calendar calendar = Calendar.getInstance();	
    Calendar answer = converter.convert(sdf.format(calendar.getTime()), Calendar.class);
    assert sdf.format(answer.getTime()).equals(sdf.format(calendar.getTime()));
  }
}
