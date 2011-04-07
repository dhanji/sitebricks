package com.google.sitebricks.example;

import com.google.common.collect.ImmutableMap;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;

/**
 * Used to ensure that the configuration works the same even without
 * annotations. We have this extra test coz some logic that distinguishes
 * web services from normal web pages relies on the presence of annotations
 * (failing explicit module config), and this ensures nothing goes haywire.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class RestfulWebServiceNoAnnotations {
  public static final String PERDIDO_STREET_STATION = "Perdido Street Station";
  public static final String CHINA_MIEVILLE = "China Mieville";
  public static final int PAGE_COUNT = 789;

  @Get
  public Reply<RestfulWebService.Book> books() {
    RestfulWebService.Book perdido = new RestfulWebService.Book();
    perdido.setAuthor(CHINA_MIEVILLE);
    perdido.setName(PERDIDO_STREET_STATION);
    perdido.setPageCount(PAGE_COUNT);

    return Reply.with(perdido)
                .headers(ImmutableMap.<String, String>of("hi", "there"))
                .type("application/json")
                .as(Json.class);
  }

  @Post
  public Reply<?> redirect() {
    return Reply.saying()
                .redirect("/other");
  }
}
