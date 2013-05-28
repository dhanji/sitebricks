package com.google.sitebricks.example;

import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Post;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

/**
 * Lets you send JSON data.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@At("/postable") @Service
public class PostableRestfulWebService {

  @Post
  public Reply<String> postBook(HttpServletRequest servletRequest, Request<String> request) {
    RestfulWebService.Book perdido = request.read(RestfulWebService.Book.class).as(Json.class);

    boolean assertions = RestfulWebService.PERDIDO_STREET_STATION.equals(perdido.getName())
        && RestfulWebService.CHINA_MIEVILLE.equals(perdido.getAuthor())
        && RestfulWebService.PAGE_COUNT == perdido.getPageCount();

    // Assert the request params are legit.
    @SuppressWarnings("unchecked")
    Map<String, String[]> map = servletRequest.getParameterMap();
    for (Map.Entry<String, String[]> entry : map.entrySet()) {
      assertions = assertions
          && Arrays.asList(entry.getValue()).equals(request.params().get(entry.getKey()));
    }

    return assertions ? Reply.with(perdido.getAuthor()) : Reply.with("failed");
  }
}
