package com.google.sitebricks.conversion;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author JRodriguez
 * @author John Patterson (jdpatterson@gmail.com)
 */
public class DateConverters {
  public static List<Class<? extends Converter<?, ?>>> converters() {
    List<Class<? extends Converter<?, ?>>> converters = new ArrayList<Class<? extends Converter<?, ?>>>();
    converters.add(LocalizedDateStringConverter.class);
    converters.add(DateLongConverter.class);
    converters.add(DateCalendarConverter.class);
    converters.add(CalendarLongConverter.class);
    converters.add(CalendarStringConverter.class);
    return converters;
  }

  public static class DateLongConverter implements Converter<Date, Long> {
    @Override
    public Date from(Long source) {
      return new Date(source);
    }

    @Override
    public Long to(Date target) {
      return target.getTime();
    }
  }
  
  public static class DateCalendarConverter implements Converter<Date, Calendar> {

    @Override
    public Calendar to(Date source) {
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(source);
      return calendar;
    }

    @Override
    public Date from(Calendar target) {
      return target.getTime();
    }
  }

  public static class DateStringConverter implements Converter<Date, String> {

    protected DateFormat format;
    
    public DateStringConverter() {
      this.format = DateFormat.getInstance();
    }

    public DateStringConverter(DateFormat format) {
      this.format = format;
    }

    public DateStringConverter(String format) {
      this.format = new SimpleDateFormat(format);
    }
    
    @Override
    public Date from(String source) {
      try {
        return getFormat().parse(source);
      }
      catch (ParseException e) {
        throw new IllegalArgumentException("Invalid date format", e);
      }
    }

    @Override
    public String to(Date target) {
      return format.format(target);
    }
    
    protected DateFormat getFormat() {
      return format;
    }
  }
  
  public static class LocalizedDateStringConverter extends DateStringConverter {
   
    private int dateStyle;
    private int timeStyle;
    private Provider<Locale> provider;
    
    public LocalizedDateStringConverter() {
      this(DateFormat.LONG, DateFormat.LONG);
    }
    public LocalizedDateStringConverter(int dateStyle, int timeStyle) {
      this.dateStyle = dateStyle;
      this.timeStyle = timeStyle;
    }
    
    @Inject(optional=true)
    public void setLocaleProvider(Provider<Locale> provider) {
      this.provider = provider;
    }
    
    @Override
    protected DateFormat getFormat() {
      if (provider != null) {
        return DateFormat.getDateTimeInstance(dateStyle, timeStyle, provider.get());
      }
      else {
        return super.getFormat();
      }
    }
  }

  public static class CalendarStringConverter implements Converter<Calendar, String> {

    private final Provider<TypeConverter> provider;
    
    @Inject
    public CalendarStringConverter(Provider<TypeConverter> provider) {
      this.provider = provider;
    }
    
    @Override
    public String to(Calendar source) {
      TypeConverter converter = provider.get();
      Date date = converter.convert(source, Date.class);
      return converter.convert(date, String.class);
    }

    @Override
    public Calendar from(String target) {
      TypeConverter converter = provider.get();
      Date date = converter.convert(target, Date.class);
      return converter.convert(date, Calendar.class);
    }
  }

  public static class CalendarLongConverter implements Converter<Calendar, Long> {

    private final Provider<TypeConverter> provider;

    @Inject
    public CalendarLongConverter(Provider<TypeConverter> provider) {
      this.provider = provider;
    }
    
    @Override
    public Long to(Calendar source) {
      TypeConverter converter = provider.get();
      Date date = converter.convert(source, Date.class);
      return converter.convert(date, Long.class);
    }

    @Override
    public Calendar from(Long target) {
      TypeConverter converter = provider.get();
      Date date = converter.convert(target, Date.class);
      return converter.convert(date, Calendar.class);
    }
  }
}
