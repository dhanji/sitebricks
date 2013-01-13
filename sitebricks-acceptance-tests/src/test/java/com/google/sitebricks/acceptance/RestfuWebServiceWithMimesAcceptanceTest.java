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

import javax.servlet.http.HttpServletResponse;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfuWebServiceWithMimesAcceptanceTest {

  public void shouldTransportJsonBothWays() {
    RestfulWebService.Book book = new RestfulWebService.Book();
    book.setAuthor("JRR Tolkien");
    book.setName("The Flobbit");
    book.setPageCount(1); // what a lousy book!

    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/mimes_service")
        .transports(RestfulWebService.Book.class)
        .over(Json.class)
        .post(book);

    assertBookResponse(response, book);
  }


	private Injector createInjector() {
		return Guice.createInjector(new AbstractModule() {
	      protected void configure() {
	        bind(ConverterRegistry.class).toInstance(new StandardTypeConverter(
              ImmutableSet.<Converter>of()));
	      }
	    });
	}


  private static void assertBookResponse(WebResponse response, RestfulWebService.Book original) {
    assert HttpServletResponse.SC_OK == response.status() : response.toString();

    // Make sure the headers were set.
    assert response.getHeaders().containsKey("Content-Type");
    assert response.getHeaders().get("Content-Type").equals("application/json");

    // assert stuff about the content itself.
    RestfulWebService.Book book = response.to(RestfulWebService.Book.class).using(Json.class);
    assert original.getAuthor().equals(book.getAuthor());
    assert original.getName().equals(book.getName());
    assert original.getPageCount() < book.getPageCount();
  }
}
