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
import com.google.sitebricks.example.RestfulWebServiceWithMatrixParams;
import org.testng.annotations.Test;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfuWebServiceWithMatrixParamsAcceptanceTest {

  public void shouldServiceTopLevelPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/matrixpath")
        .transports(String.class)
        .over(Json.class)
        .get();

    assert RestfulWebServiceWithMatrixParams.TOPLEVEL.equals(response.toString())
        : response.toString();
  }

  public void shouldServiceVariableThreeLevelSubPath() {
    WebResponse response = createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.baseUrl() + "/matrixpath/val;param1=val1;param2=val2/athing")
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert ("{param1=[val1], param2=[val2]}"  // Multimap of matrix params
        + "_" + "val;param1=val1;param2=val2" // Variable path fragment #1
        + "_" + "athing")                     // Variable path fragment #2
        .equals(response.toString()) : response.toString();
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
