package com.google.sitebricks.acceptance.util;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import com.google.sitebricks.SitebricksModule;

/**
 * Abstract TestNG/JUnit4 test that automatically binds and injects itself.
 */
public abstract class SitebricksServiceTest implements Module {

  // ----------------------------------------------------------------------
  // Implementation fields
  // ----------------------------------------------------------------------

  private String basedir;
  private Injector injector;
  private Jetty server;

  // ----------------------------------------------------------------------
  // Setup
  // ----------------------------------------------------------------------

  @BeforeSuite
  public void beforeSuite() throws Exception {
    //
    // Find a free port for the tests
    //
    server = new Jetty("src/test/webapp", 0);
    server.start();
  }

  @AfterSuite
  public void afterSuite() throws Exception {
    server.stop();
  }

  @Before
  @BeforeMethod
  public void setUp() {
    injector = Guice.createInjector(new SetUpModule(), sitebricksModule());
  }

  protected SitebricksModule sitebricksModule() {
    return new SitebricksModule();
  }

  @After
  @AfterMethod
  public void tearDown() {
  }

  final class SetUpModule implements Module {
    public void configure(final Binder binder) {
      binder.install(SitebricksServiceTest.this);
      binder.requestInjection(SitebricksServiceTest.this);
    }
  }

  // ----------------------------------------------------------------------
  // Container configuration methods
  // ----------------------------------------------------------------------

  /**
   * Custom injection bindings.
   *
   * @param binder
   *          The Guice binder
   */
  public void configure(final Binder binder) {
    // place any per-test bindings here...
  }

  /**
   * Custom property values.
   *
   * @param properties
   *          The test properties
   */
  public void configure(final Properties properties) {
    // put any per-test properties here...
  }

  // ----------------------------------------------------------------------
  // Container lookup methods
  // ----------------------------------------------------------------------

  public final <T> T lookup(final Class<T> type) {
    return lookup(Key.get(type));
  }

  public final <T> T lookup(final Class<T> type, final String name) {
    return lookup(type, Names.named(name));
  }

  public final <T> T lookup(final Class<T> type, final Class<? extends Annotation> qualifier) {
    return lookup(Key.get(type, qualifier));
  }

  public final <T> T lookup(final Class<T> type, final Annotation qualifier) {
    return lookup(Key.get(type, qualifier));
  }

  // ----------------------------------------------------------------------
  // Container resource methods
  // ----------------------------------------------------------------------

  public final String getBasedir() {
    if (null == basedir) {
      basedir = System.getProperty("basedir", new File("").getAbsolutePath());
    }
    return basedir;
  }

  // ----------------------------------------------------------------------
  // Implementation methods
  // ----------------------------------------------------------------------

  private final <T> T lookup(final Key<T> key) {
    return injector.getInstance(key);
  }

  protected String baseUrl() {
    return "http://localhost:" + server.getListeningPort() + "/sitebricks";
  }
}
