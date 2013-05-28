package com.google.sitebricks.example;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Injector;
import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.http.Post;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@At("/service") @Service
public class RestfulWebService {
  public static final String PERDIDO_STREET_STATION = "Perdido Street Station";
  public static final String CHINA_MIEVILLE = "China Mieville";
  public static final int PAGE_COUNT = 789;

  @Get
  public Reply<Book> books(Injector injector, Request<String> request,
                       @SitebricksConfig.Test Start start) {
    Preconditions.checkNotNull(injector, "method argument injection failed");
    Preconditions.checkNotNull(request, "method argument injection failed");
    Preconditions.checkNotNull(start, "method argument injection failed");

    Book perdido = new Book();
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


  /**
   * A data model object, or "Entity" that we will use to
   * generate the reply. This can be any Java object and
   * typically will not be an inner class like this one.
   */
  public static class Book {
    private String name;
    private String author;
    private int pageCount;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getAuthor() {
      return author;
    }

    public void setAuthor(String author) {
      this.author = author;
    }

    public int getPageCount() {
      return pageCount;
    }

    public void setPageCount(int pageCount) {
      this.pageCount = pageCount;
    }
  }
}
