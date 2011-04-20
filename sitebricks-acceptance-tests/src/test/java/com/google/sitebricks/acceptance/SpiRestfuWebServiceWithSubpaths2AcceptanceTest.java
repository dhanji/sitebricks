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
public class SpiRestfuWebServiceWithSubpaths2AcceptanceTest {

  public void shouldServiceTopLevelDynamicPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/spi/test")
        .transports(String.class)
        .over(Json.class)
        .get();

    assert "get:top".equals(response.toString());
  }

  public void shouldServiceFirstLevelStaticPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/spi/test")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert "post:junk_subpath1".equals(response.toString()) : response.toString();
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
