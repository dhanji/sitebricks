package com.google.sitebricks.headless;

import com.google.sitebricks.client.transport.Json;
import org.testng.annotations.Test;

/**
 * A test for the reply builder api.
 */
public class ReplyEdslTest {

  @Test
  public final void entities() {

    // Our default transport will convert this to a plain text string.
    Reply.with(new Object());

    // Serialized explicitly with a transport.
    Reply.with(new Object()).as(Json.class);

    // Simple plain text response (the default).
    Reply.with("hello there!");

    

  }

  @Test
  public final void replies() {

    //200s
    Reply.saying()
        .noContent();  // 204


    // redirects
    Reply.saying()
         .seeOther("/other");   // 303

    Reply.saying()
         .redirect("http://other.com/stuff"); // 302


    // 400s
    Reply.saying()
         .notFound();  // 404

    Reply.saying()
         .unauthorized();  // 401

    Reply.saying()
         .forbidden();  // 403



    // others
    Reply.saying()
         .error();  // 500
  }
}
