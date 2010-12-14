package com.google.inject.stat;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class StatsIntegrationTest {
  @Test
  public final void testRecordingStatsInAnyGuiceObject() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        install(new StatModule("/stat"));

        bind(DummyService.class);
      }
    });

    DummyService service = injector.getInstance(DummyService.class);

    service.call();
    service.call();
    service.call();

    assertEquals(3, service.calls.get());

    Stats stats = injector.getInstance(Stats.class);
    Map<String,String> snapshot = stats.snapshot();

    assertEquals(1, snapshot.size());
    String numCalls = snapshot.get(DummyService.NUMBER_OF_CALLS);
    assertNotNull(numCalls);
    assertEquals("3", numCalls);
  }

  @Singleton
  public static class DummyService {
    private static final String NUMBER_OF_CALLS = "number-of-calls";

    @Stat(NUMBER_OF_CALLS) AtomicInteger calls = new AtomicInteger();

    public void call() {
      calls.incrementAndGet();
    }
  }
}
