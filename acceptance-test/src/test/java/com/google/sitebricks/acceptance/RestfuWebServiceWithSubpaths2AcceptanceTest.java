package com.google.sitebricks.acceptance;

import com.google.inject.Guice;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.example.RestfulWebServiceWithSubpaths2;
import org.testng.annotations.Test;

import static com.google.sitebricks.example.RestfulWebServiceWithSubpaths2.TOPLEVEL;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfuWebServiceWithSubpaths2AcceptanceTest {

  public void shouldServiceTopLevelDynamicPath() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/" + TOPLEVEL)
        .transports(String.class)
        .over(Json.class)
        .get();

    assert TOPLEVEL.equals(response.toString());
  }

  public void shouldServiceFirstLevelStaticPath() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/subpath1")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert RestfulWebServiceWithSubpaths2.PATH_1.equals(response.toString()) : response.toString();
  }

  public void shouldServiceTwoLevelDynamicPath() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/more_junk")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert "junk_more_junk".equals(response.toString()) : response.toString();
  }
}
