package info.sitebricks.web;

import com.google.inject.name.Named;
import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/:page")
public class Home {
  @Get
  void showWikiPage(@Named("page") String name) {
    // Look up page by name, then render it with markdownj.
    
  }
}
