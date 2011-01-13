package com.google.inject.stat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.stat.testservices.DummyService;


import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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

    assertEquals(3, service.getCalls().intValue());

    Stats stats = injector.getInstance(Stats.class);
    Map<StatDescriptor, Object> snapshot = stats.snapshot();

    assertEquals(1, snapshot.size());
    Map.Entry<StatDescriptor, Object> entry =
        snapshot.entrySet().iterator().next();

    StatDescriptor statDescriptor = entry.getKey();
    assertEquals(DummyService.NUMBER_OF_CALLS, statDescriptor.getName());
    Object value = entry.getValue();
    // This assertion also checks that the value is the true underlying value
    // of the stat, and not only a string representation.
    AtomicInteger atomicIntegerValue = (AtomicInteger) value;
    assertEquals(3, atomicIntegerValue.get());
  }
}
