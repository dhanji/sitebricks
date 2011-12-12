package com.google.sitebricks.slf4j;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class Slf4jIntegrationTest {
  @Test
  public final void testLoggerInjection() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        install(new Slf4jModule());
      }
    });

    // simple case
    AService instance = injector.getInstance(AService.class);

    assertNotNull(instance.log);
    assertEquals(AService.class.getName(), instance.log.getName());

    // Expect no exception thrown.
    instance.log.debug("Works!");

    // inherited logger
    TheService inheritedInstance = injector.getInstance(TheService.class);

    assertNotNull(inheritedInstance.log);
    assertEquals(TheService.class.getName(),
        inheritedInstance.log.getName());

    // Expect no exception thrown.
    inheritedInstance.log.debug("Works!");
  }

  public static class AbstractService {
    Logger log;
  }

  public static class TheService extends AbstractService {
  }

  public static class AService {
    Logger log;
  }
}
