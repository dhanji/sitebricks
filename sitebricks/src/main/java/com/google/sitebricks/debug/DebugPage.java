package com.google.sitebricks.debug;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import com.google.sitebricks.At;
import com.google.sitebricks.http.Get;
import com.google.sitebricks.routing.PageBook;
import com.google.sitebricks.routing.PageBook.Page;

import java.util.Collections;
import java.util.List;

/**
 * Page showing some stats about current sitebricks configuration.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@At("/debug") @RequestScoped
public class DebugPage {
  @Inject
  private PageBook pageBook;

  private List<Page> resources;
  private List<Page> pages;

  @Get
  void debug() {
    resources = Lists.newArrayList();
    pages = Lists.newArrayList();
    for (List<Page> pages : pageBook.getPageMap()) {
      for (Page page : pages) {
        if (page.isHeadless()) {
          resources.add(page);
        } else {
          this.pages.add(page);
        }
      }
    }

    // O(n log n)
    Collections.sort(resources);
    Collections.sort(pages);
  }

  public List<Page> getResources() {
    return resources;
  }

  public List<Page> getPages() {
    return pages;
  }
}
