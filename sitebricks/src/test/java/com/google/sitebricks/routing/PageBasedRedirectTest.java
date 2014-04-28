package com.google.sitebricks.routing;

import com.google.common.collect.ImmutableMap;
import com.google.inject.util.Providers;
import com.google.sitebricks.At;
import com.google.sitebricks.headless.Request;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class PageBasedRedirectTest {
  private PageBasedRedirect redirect;

  @At("/:product/list/:id")
  public static class ProductIdList {

  }

  @At("/product/list/id")
  public static class StaticPage {

  }

  public static class NonPage {

  }

  @BeforeMethod
  public void pre() {
    redirect = new PageBasedRedirect();

    Request requestMock = createNiceMock(Request.class);
    redirect.setRequestProvider(Providers.of(requestMock));

    replay(requestMock);
  }

  @Test
  public void fillInUriTemplate() throws Exception {
    String uri = redirect.to(ProductIdList.class, ImmutableMap.of(
        "product", "Indie Game",
        "id", "Super Meat Boy"));

    assert "/Indie+Game/list/Super+Meat+Boy".equals(uri) : uri;
  }

  @Test
  public void staticUri() throws Exception {
    String uri = redirect.to(StaticPage.class, ImmutableMap.of(
        "product", "Indie Game",
        "id", "Super Meat Boy"));

    assert "/product/list/id".equals(uri) : uri;
    assert "/product/list/id".equals(redirect.to(StaticPage.class)) : uri;
  }

  @Test
  public void staticUriContextualized() throws Exception {
    Request requestMock = createMock(Request.class);
    expect(requestMock.context()).andReturn("/bricks").anyTimes();
    redirect.setRequestProvider(Providers.of(requestMock));

    replay(requestMock);

    String uri = redirect.to(StaticPage.class, ImmutableMap.of(
        "product", "Indie Game",
        "id", "Super Meat Boy"));

    assert "/bricks/product/list/id".equals(uri) : uri;
    assert "/bricks/product/list/id".equals(redirect.to(StaticPage.class)) : uri;
  }

  @Test
  public void fillInUriTemplateContextualized() throws Exception {
    Request requestMock = createMock(Request.class);
    expect(requestMock.context()).andReturn("/bricks").anyTimes();
    redirect.setRequestProvider(Providers.of(requestMock));

    replay(requestMock);

    String uri = redirect.to(ProductIdList.class, ImmutableMap.of(
        "product", "Indie Game",
        "id", "Super Meat Boy"));

    assert "/bricks/Indie+Game/list/Super+Meat+Boy".equals(uri) : uri;
  }


  @Test(expectedExceptions = IllegalArgumentException.class)
  public void badParameters() throws Exception {
    String uri = redirect.to(ProductIdList.class, ImmutableMap.of(
        "id", "Super Meat Boy"));

    assert "/Indie+Game/list/Super+Meat+Boy".equals(uri) : uri;
  }
}
