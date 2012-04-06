package com.google.sitebricks.example;

import com.google.sitebricks.conversion.Converter;

public class CustomToStringConverter implements Converter<String, Custom> {

  @Override
  public Custom to(String source) {
    return new Custom(source);
  }

  @Override
  public String from(Custom target) {
    return target.toString();
  }

}
