package com.google.sitebricks.acceptance;

import com.google.inject.Guice;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;
import org.testng.annotations.Test;

import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.BASE_SERVICE_PATH;
import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.CREATE;
import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.DELETE;
import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.READ_COLLECTION;
import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.READ_INDIVIDUAL;
import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.UPDATE;

/**
 * @author Jason van Zyl
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfuWebServiceWithCRUDAcceptanceTest {

  public void create() {
  	String url = AcceptanceTest.BASE_URL + BASE_SERVICE_PATH;
  	System.out.println("POST " + url);
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(url)
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert CREATE.equals(response.toString()) : response.toString();
  }

  public void readCollection() {
  	String url = AcceptanceTest.BASE_URL + BASE_SERVICE_PATH;
  	System.out.println("GET " + url);
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(url)
        .transports(String.class)
        .over(Json.class)
        .get();

    assert READ_COLLECTION.equals(response.toString());
  }  

  public void readIndividual() {
  	String url = AcceptanceTest.BASE_URL + BASE_SERVICE_PATH  + "/1";
  	System.out.println("GET " + url);
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(url)
        .transports(String.class)
        .over(Json.class)
        .get();

    assert READ_INDIVIDUAL.equals(response.toString()) : response.toString();
  }  
  
  public void update() {
  	String url = AcceptanceTest.BASE_URL + BASE_SERVICE_PATH  + "/1";
  	System.out.println("PUT " + url);
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(url)
        .transports(String.class)
        .over(Json.class)
        .put("");

    assert UPDATE.equals(response.toString());
  }

  public void delete() {
  	String url = AcceptanceTest.BASE_URL + BASE_SERVICE_PATH  + "/1";
  	System.out.println("DELETE " + url);
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(url)
        .transports(String.class)
        .over(Json.class)
        .delete();

    assert DELETE.equals(response.toString());
  }
}
