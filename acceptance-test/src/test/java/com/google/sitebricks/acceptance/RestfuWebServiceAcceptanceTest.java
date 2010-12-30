package com.google.sitebricks.acceptance;

import com.google.inject.Guice;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.client.transport.Text;
import com.google.sitebricks.example.RestfulWebService;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfuWebServiceAcceptanceTest {

  public void shouldTransportJsonWithoutTemplate() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/service")
        .transports(String.class)
        .over(Json.class)
        .get();

    assertBookResponse(response);
  }

  public void shouldRedirect() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/service")
        .transports(String.class)
        .over(Text.class)
        .post("");

    assertRedirectResponse(response);
  }

  public void shouldTransportJsonWithoutTemplateNoAnnotations() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/no_annotations/service")
        .transports(String.class)
        .over(Json.class)
        .get();

    assertBookResponse(response);
  }

  public void shouldRedirectNoAnnotations() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/no_annotations/service")
        .transports(String.class)
        .over(Text.class)
        .post("");

    assertRedirectResponse(response);
  }

  private static void assertRedirectResponse(WebResponse response) {
    assert HttpServletResponse.SC_MOVED_TEMPORARILY == response.status() : response.toString();
    assert response.getHeaders().get("Location").endsWith("/other");
  }

  private static void assertBookResponse(WebResponse response) {
    assert HttpServletResponse.SC_OK == response.status();

    // Make sure the headers were set.
    assert response.getHeaders().containsKey("hi");
    assert "there".equals(response.getHeaders().get("hi"));
    assert response.getHeaders().containsKey("Content-Type");

    // assert stuff about the content itself.
    RestfulWebService.Book book = response.to(RestfulWebService.Book.class).using(Json.class);
    assert RestfulWebService.CHINA_MIEVILLE.equals(book.getAuthor());
    assert RestfulWebService.PERDIDO_STREET_STATION.equals(book.getName());
    assert RestfulWebService.PAGE_COUNT == book.getPageCount();
  }
}
