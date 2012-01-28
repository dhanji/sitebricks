package org.sitebricks.client.easy;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sitebricks.client.easy.SisuSitebricksClientModule.restClientModule;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.mockito.Mockito;
import org.sitebricks.client.easy.service.FooService;
import org.testng.annotations.Test;

import com.google.sitebricks.SitebricksModule;
import com.google.sitebricks.acceptance.util.SitebricksServiceTest;

@Test(suiteName = "easyClientSuite")
public class SisuSitebricksClientModuleTest extends SitebricksServiceTest {

  @Inject
  private RestClientFactory<Foo> fooFactory;

  private Foo mockFoo;

  @Override
  protected SitebricksModule sitebricksModule() {
    return new SitebricksModule() {
      @Override
      protected void configureSitebricks() {
        mockFoo = mock(Foo.class);
        bind(Foo.class).toInstance(mockFoo);
        install(restClientModule().bind("echo").to("Sitebricks").build(Foo.class));        
        scan(FooService.class.getPackage());
      } 
    };
  }  
  
  @Test
  public void get() throws Exception {
    final Bar bar = new Bar();
    final List<Bar> mockBars = Arrays.asList(bar);

    when(mockFoo.get()).thenReturn(mockBars);

    final Foo foo = fooFactory.create(new URL(baseUrl()));
    final Collection<Bar> bars = foo.get();

    verify(mockFoo).get();
    assertThat("Bars", new ArrayList<Bar>(bars), is(equalTo(new ArrayList<Bar>(mockBars))));
  }

  @Test
  public void getById() throws Exception {
    final Bar mockBar = new Bar();

    when(mockFoo.get("1")).thenReturn(mockBar);

    final Foo foo = fooFactory.create(new URL(baseUrl()));
    final Bar bar = foo.get("1");

    verify(mockFoo).get("1");
    assertThat("Bar", bar, is(equalTo(mockBar)));
  }

  @Test
  public void add() throws Exception {
    final Bar mockBar = new Bar();

    final Foo foo = fooFactory.create(new URL(baseUrl()));
    foo.add(mockBar);

    verify(mockFoo).add(Mockito.any(Bar.class));
  }

  @Test
  public void remove() throws Exception {
    final Foo foo = fooFactory.create(new URL(baseUrl()));
    foo.remove("1");

    verify(mockFoo).remove("1");
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void missingAnnotation() throws Exception {
    final Foo foo = fooFactory.create(new URL(baseUrl()));
    foo.missingAnnotation();
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void missingBinding() throws Exception {
    final Foo foo = fooFactory.create(new URL(baseUrl()));
    foo.missingBinding();
  }

  @Test
  public void echo() throws Exception {
    final Foo foo = fooFactory.create(new URL(baseUrl()));
    final String echo = foo.echo("hello");

    assertThat("Echo message", echo, is(equalTo("Sitebricks says hello!")));
  }

  @Test
  public void echoWithPost() throws Exception {
    final Foo foo = fooFactory.create(new URL(baseUrl()));
    final String echo = foo.echoWithPost("hello");

    assertThat("Echo message", echo, is(equalTo("Sitebricks says hello!")));
  }  
}
