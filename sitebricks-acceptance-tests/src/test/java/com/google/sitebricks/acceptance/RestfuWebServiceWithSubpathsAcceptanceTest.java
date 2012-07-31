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
import com.google.sitebricks.example.RestfulWebServiceWithSubpaths;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfuWebServiceWithSubpathsAcceptanceTest {

  public void shouldServiceTopLevelPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/superpath")
        .transports(String.class)
        .over(Json.class)
        .get();

    assert RestfulWebServiceWithSubpaths.TOPLEVEL.equals(response.toString());
  }

  public void shouldServiceFirstSubPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/superpath/subpath1")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert RestfulWebServiceWithSubpaths.PATH_1.equals(response.toString()) : response.toString();
  }

  public void shouldServiceSecondSubPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/superpath/subpath2")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert RestfulWebServiceWithSubpaths.PATH_2.equals(response.toString()) : response.toString();
  }

  public void shouldServiceThirdSubPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/superpath/subpath3")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert RestfulWebServiceWithSubpaths.PATH_3.equals(response.toString()) : response.toString();
  }

  public void shouldServiceVariableTwoLevelSubPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/superpath/subpath1/a_thing")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert "a_thing".equals(response.toString()) : response.toString();
  }

  public void shouldServiceVariableThreeLevelSubPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/superpath/subpath1/a_thing/another_thing")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert "a_thing_another_thing".equals(response.toString()) : response.toString();
  }

  public void shouldServiceVariableTwoLevelSubPath2() {
    String aString = "aoskdoaksd" + new Date().hashCode();
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/superpath/subpath3/" + aString)
        .transports(String.class)
        .over(Json.class)
        .post("");

    // Should be reflected
    assert aString.equals(response.toString()) : response.toString();
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
