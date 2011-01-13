package com.google.inject.slf4j;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import org.slf4j.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    AService instance = injector.getInstance(AService.class);

    assertNotNull(instance.log);
    assertEquals(AService.class.getName(), instance.log.getName());

    // Expect no exception thrown.
    instance.log.debug("Works!");
  }

  public static class AService {
    Logger log;
  }
}
