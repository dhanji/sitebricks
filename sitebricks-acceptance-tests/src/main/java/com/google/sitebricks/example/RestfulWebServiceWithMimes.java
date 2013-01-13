package com.google.sitebricks.example;

import com.google.sitebricks.At;
import com.google.sitebricks.client.transport.Json;
import com.google.sitebricks.headless.Reply;
import com.google.sitebricks.headless.Service;
import com.google.sitebricks.http.As;
import com.google.sitebricks.http.Post;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@At("/mimes_service") @Service
public class RestfulWebServiceWithMimes {
  public static final String PERDIDO_STREET_STATION = "Perdido Street Station";
  public static final String CHINA_MIEVILLE = "China Mieville";
  public static final int PAGE_COUNT = 789;

  @Post
  @As(Json.class) Reply<RestfulWebService.Book> latestEditionOf(@As(Json.class) RestfulWebService.Book edition) {
    edition.setPageCount(9999);

    return Reply.with(edition);
  }
}
