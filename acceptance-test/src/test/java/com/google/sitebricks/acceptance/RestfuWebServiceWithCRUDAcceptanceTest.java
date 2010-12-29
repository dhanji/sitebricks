package com.google.sitebricks.acceptance;

import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.BASE_SERVICE_PATH;
import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.CREATE;
import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.DELETE;
import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.READ_COLLECTION;
import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.READ_INDIVIDUAL;
import static com.google.sitebricks.example.RestfulWebServiceWithCRUD.UPDATE;

import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.sitebricks.acceptance.util.AcceptanceTest;
import com.google.sitebricks.client.Web;
import com.google.sitebricks.client.WebResponse;
import com.google.sitebricks.client.transport.Json;

/**
 * @author Jason van Zyl
 */
@Test(suiteName = AcceptanceTest.SUITE)
public class RestfuWebServiceWithCRUDAcceptanceTest {

  public void create() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + BASE_SERVICE_PATH )
        .transports(String.class)
        .over(Json.class)
        .post("");

    assert CREATE.equals(response.toString()) : response.toString();
  }

  public void readCollection() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + BASE_SERVICE_PATH )
        .transports(String.class)
        .over(Json.class)
        .get();

    assert READ_COLLECTION.equals(response.toString());
  }  

  public void readIndividual() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + BASE_SERVICE_PATH  + "/1" )
        .transports(String.class)
        .over(Json.class)
        .get();

    assert READ_INDIVIDUAL.equals(response.toString());
  }  
  
  public void update() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + BASE_SERVICE_PATH  + "/1" )
        .transports(String.class)
        .over(Json.class)
        .put("");

    assert UPDATE.equals(response.toString());
  }

  public void delete() {
    WebResponse response = Guice.createInjector()
        .getInstance(Web.class)
        .clientOf(AcceptanceTest.BASE_URL + BASE_SERVICE_PATH  + "/1" )
        .transports(String.class)
        .over(Json.class)
        .delete();

    assert DELETE.equals(response.toString());
  }
}
