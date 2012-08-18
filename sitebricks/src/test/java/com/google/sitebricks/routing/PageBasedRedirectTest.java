package com.google.sitebricks.routing;

import com.google.common.collect.ImmutableMap;
import com.google.sitebricks.At;
import org.testng.annotations.Test;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class PageBasedRedirectTest {

  @At("/:product/list/:id")
  public static class ProductIdList {

  }

  @At("/product/list/id")
  public static class StaticPage {

  }

  public static class NonPage {

  }

  @Test
  public void fillInUriTemplate() throws Exception {
    String uri = new PageBasedRedirect().to(ProductIdList.class, ImmutableMap.of(
        "product", "Indie Game",
        "id", "Super Meat Boy"));

    assert "/Indie+Game/list/Super+Meat+Boy".equals(uri) : uri;
  }

  @Test
  public void staticUri() throws Exception {
    String uri = new PageBasedRedirect().to(StaticPage.class, ImmutableMap.of(
        "product", "Indie Game",
        "id", "Super Meat Boy"));

    assert "/product/list/id".equals(uri) : uri;
    assert "/product/list/id".equals(new PageBasedRedirect().to(StaticPage.class)) : uri;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void badParameters() throws Exception {
    String uri = new PageBasedRedirect().to(ProductIdList.class, ImmutableMap.of(
        "id", "Super Meat Boy"));

    assert "/Indie+Game/list/Super+Meat+Boy".equals(uri) : uri;
  }
}
