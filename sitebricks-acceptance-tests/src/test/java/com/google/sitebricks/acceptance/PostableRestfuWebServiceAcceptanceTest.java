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
import com.google.sitebricks.example.RestfulWebService;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class PostableRestfuWebServiceAcceptanceTest {

  public void shouldTransportJsonWithoutTemplate() {
    RestfulWebService.Book perdido = new RestfulWebService.Book();
    perdido.setAuthor(RestfulWebService.CHINA_MIEVILLE);
    perdido.setName(RestfulWebService.PERDIDO_STREET_STATION);
    perdido.setPageCount(RestfulWebService.PAGE_COUNT);


    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/postable?p1=v1,v2")
        .transports(RestfulWebService.Book.class)
        .over(Json.class)
        .post(perdido);

    // Should ping us the author back.
    assert response.toString().contains(perdido.getAuthor()) : response.toString();
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
