package com.google.sitebricks.acceptance;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.conversion.Converter;
import com.google.sitebricks.conversion.ConverterRegistry;
import com.google.sitebricks.conversion.StandardTypeConverter;
import com.google.sitebricks.example.RestfulWebServiceWithGenerics.Person;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Miroslav Genov (mgenov@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfulWebServiceWithGenericsAcceptanceTest {

  public void whatWasPostedIsReturnedAsResponse() {
    List<Person> personList = Lists.newArrayList(new Person("John Smith"));

    WebResponse response = createInjector()
            .getInstance(Web.class)
            .clientOf(AcceptanceTest.baseUrl() + "/serviceWithGenerics")
            .transports(new TypeLiteral<List<Person>>() { })
            .over(Json.class)
            .post(personList);

    assert HttpServletResponse.SC_OK == response.status();

    List<Person> result = response.to(new TypeLiteral<List<Person>>() {}).using(Json.class);
    assert result.size() == 1;
    assert "John Smith".equals(result.get(0).getName());
  }


  private Injector createInjector() {
    return Guice.createInjector(new AbstractModule() {
      protected void configure() {
        bind(ConverterRegistry.class).toInstance(new StandardTypeConverter(
                ImmutableSet.<Converter>of()));
      }
    });
  }


}
