package com.google.sitebricks.conversion;

import java.math.BigDecimal;
import java.util.Date;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.SitebricksModule;

public class TypeConverterTest {
  
  private TypeConverter converter;

  @BeforeTest
  public void setup() {
    Injector injector = Guice.createInjector(new SitebricksModule());
    converter = injector.getInstance(TypeConverter.class);
  }

  @Test
  public void stringToPrimitive() {
    int answer = converter.convert("42", Integer.class);
    assert answer == 42;
  }

  @Test
  public void numbers() {
    BigDecimal answer = converter.convert(42, BigDecimal.class);
    assert answer.intValue() == 42;
  }
  
  @Test
  public void dateToString() {
    String answer = converter.convert(new Date(), String.class);
    assert answer.length() > 0;
  }
}
