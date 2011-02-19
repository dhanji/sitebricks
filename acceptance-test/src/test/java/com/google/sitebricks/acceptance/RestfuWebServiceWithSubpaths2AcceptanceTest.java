package com.google.sitebricks.acceptance;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.conversion.ConverterRegistry;
import com.google.sitebricks.conversion.StandardTypeConverter;
import com.google.sitebricks.example.RestfulWebServiceWithSubpaths2;
import org.testng.annotations.Test;

import static com.google.sitebricks.example.RestfulWebServiceWithSubpaths2.TOPLEVEL;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfuWebServiceWithSubpaths2AcceptanceTest {

  public void shouldServiceTopLevelDynamicPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/" + TOPLEVEL)
        .transports(String.class)
        .over(Json.class)
        .get();

    assert TOPLEVEL.equals(response.toString());
  }

  public void shouldServiceFirstLevelStaticPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/subpath1")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert RestfulWebServiceWithSubpaths2.PATH_1.equals(response.toString()) : response.toString();
  }

  public void shouldServiceSameFirstLevelStaticPathWithPutMethod() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/subpath1")
        .transports(String.class)
        .over(Json.class)
        .put("");

    assert RestfulWebServiceWithSubpaths2.PATH_1_PUT.equals(response.toString())
        : response.toString();
  }

  public void shouldServiceSameFirstLevelStaticPathWithDeleteMethod() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/subpath1")
        .transports(String.class)
        .over(Json.class)
        .delete();

    assert RestfulWebServiceWithSubpaths2.PATH_1_DELETE.equals(response.toString())
        : response.toString();
  }

  public void shouldServiceTwoLevelDynamicPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/more_junk")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert "junk_more_junk".equals(response.toString()) : response.toString();
  }

  public void shouldServiceTwoLevelDynamicPathWithDeleteMethod() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/more_junk")
        .transports(String.class)
        .over(Json.class)
        .delete();

    assert "delete:junk_more_junk".equals(response.toString()) : response.toString();
  }

  public void shouldServiceThreeLevelDynamicPathWithDeleteMethod() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/more_junk/most_junk")
        .transports(String.class)
        .over(Json.class)
        .delete();

    assert "delete:junk_more_junk_most_junk".equals(response.toString()) : response.toString();
  }

  public void shouldServiceThreeLevelDynamicPathWithPutMethod() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/more_junk/most_junk")
        .transports(String.class)
        .over(Json.class)
        .put("");

    assert "put:junk_more_junk_most_junk".equals(response.toString()) : response.toString();
  }

  public void shouldServiceThreeLevelDynamicPathWithPostMethod() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/more_junk/most_junk")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert "post:junk_more_junk_most_junk".equals(response.toString()) : response.toString();
  }

  public void shouldServiceThreeLevelDynamicPathWithGetMethod() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/more_junk/most_junk")
        .transports(String.class)
        .over(Json.class)
        .get();

    assert "get:junk_more_junk_most_junk".equals(response.toString()) : response.toString();
  }
  
	private Injector createInjector() {
		return Guice.createInjector(new AbstractModule() {
	      protected void configure() {
	        bind(ConverterRegistry.class).toInstance(new StandardTypeConverter());
	      }
	    });
	}
//
//  public void shouldService4LevelMixedPathWithGetMethod() {
//    WebResponse response = createInjector()
//        .getInstance(Web.class)
//        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/more_junk/most_junk/4l")
//        .transports(String.class)
//        .over(Json.class)
//        .get();
//
//    assert "4l:get:junk_more_junk_most_junk".equals(response.toString()) : response.toString();
//  }
//
//  public void shouldService4LevelMixedPathWithPostMethod() {
//    WebResponse response = createInjector()
//        .getInstance(Web.class)
//        .clientOf(AcceptanceTest.BASE_URL + "/superpath2/junk/more_junk/most_junk/4l")
//        .transports(String.class)
//        .over(Json.class)
//        .post("");
//
//    assert "4l:post:junk_more_junk_most_junk".equals(response.toString()) : response.toString();
//  }
}
