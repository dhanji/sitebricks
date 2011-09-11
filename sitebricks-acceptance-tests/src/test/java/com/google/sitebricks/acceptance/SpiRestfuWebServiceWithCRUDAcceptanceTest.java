package com.google.sitebricks.acceptance;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.conversion.Converter;
import com.google.sitebricks.conversion.ConverterRegistry;
import com.google.sitebricks.conversion.StandardTypeConverter;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class SpiRestfuWebServiceWithCRUDAcceptanceTest {

  public void shouldServiceReadCollection() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/issue")
        .transports(String.class)
        .over(Json.class)
        .get();

    assert "READ".equals(response.toString());
  }

  public void shouldServiceRead() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/issue/0")
        .transports(String.class)
        .over(Json.class)
        .get();

    assert "READ".equals(response.toString());
  }
  
  public void shouldServiceCreate() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/issue")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert "CREATE".equals(response.toString()) : response.toString();
  }

  public void shouldServiceUpdate() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/issue/0")
        .transports(String.class)
        .over(Json.class)
        .put("");

    assert "UPDATE".equals(response.toString()) : response.toString();
  }
  
  public void shouldServiceDelete() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/issue/0")
        .transports(String.class)
        .over(Json.class)
        .delete();

    assert "DELETE".equals(response.toString()) : response.toString();
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
