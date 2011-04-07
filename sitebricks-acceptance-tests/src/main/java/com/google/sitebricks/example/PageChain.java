package com.google.sitebricks.example;

import com.google.sitebricks.At;
import com.google.sitebricks.http.Post;

/**
 * Demonstrates passing state between pages, without
 * leaking it to the client or using a persistent datastore.
 *
 * @author dhanji@google.com (Dhanji R. Prasanna)
 */
@At("/pagechain")
public class PageChain {
  private String userValue;

  @Post NextPage redirect() {

    // Redirect to nextpage and use this provided instance,
    // that way we pass the custom value thru.
    return new NextPage(userValue);
  }

  public void setUserValue(String userValue) {
    this.userValue = userValue;
  }
}
