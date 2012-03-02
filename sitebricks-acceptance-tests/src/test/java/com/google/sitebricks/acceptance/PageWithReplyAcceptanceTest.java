package com.google.sitebricks.acceptance;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.testng.annotations.Test;

import com.google.sitebricks.acceptance.util.AcceptanceTest;

@Test(suiteName = AcceptanceTest.SUITE)
public class PageWithReplyAcceptanceTest {

  public void shouldReturnCustomStatusCode() throws IOException {
    URL url = new URL(AcceptanceTest.BASE_URL + "/pageWithReply");
    
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    int expected = 678;
    int actual = connection.getResponseCode();

    assert actual == expected : "expected custom response code '" + expected + "' but was '" + actual + "'";
  }
}
