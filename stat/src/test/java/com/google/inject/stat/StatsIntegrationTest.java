package com.google.inject.stat;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.stat.testservices.ChildDummyService;
import com.google.inject.stat.testservices.DummyService;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class StatsIntegrationTest {
  Injector injector;

  @Before public void setUp() {
    injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        install(new StatModule("/stat"));

        bind(DummyService.class);
        bind(ChildDummyService.class);
      }
    });
  }

  @Test public final void testPublishingStatsInDummyService() {
    DummyService service = injector.getInstance(DummyService.class);

    service.call();
    service.call();
    service.call();

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();

    assertEquals(2, snapshot.size());

    // Here we check the value of the field, NUMBER_OF_CALLS
    StatDescriptor numberOfCallsDescriptor =
        getByName(DummyService.NUMBER_OF_CALLS, snapshot);
    Object numberOfCallsValue = snapshot.get(numberOfCallsDescriptor);

    // This assertion also checks that the value is the true underlying value
    // of the stat, and not only a string representation.
    AtomicInteger numberOfCalls = (AtomicInteger) numberOfCallsValue;
    assertEquals(service.getCalls().intValue(), numberOfCalls.get());

    // Here we check the value of the method, CALL_LATENCY_NS
    StatDescriptor callLatencyNsDescriptor =
        getByName(DummyService.CALL_LATENCY_NS, snapshot);
    Object callLatencyNsValue = snapshot.get(callLatencyNsDescriptor);
    Long callLatencyNs = (Long) callLatencyNsValue;
    assertEquals(service.getCallLatencyMs(), callLatencyNs);
  }

  /**
   * This test illustrates how stats are not published from parent classes.
   * Adding such behavior is a TODO, as it most likely would be the expected
   * behavior.
   */
  @Test public void testPublishingStatsInChildService() {
    ChildDummyService service = injector.getInstance(ChildDummyService.class);

    service.call();
    service.call();

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();

    assertEquals(1, snapshot.size());
    StatDescriptor numberOfChildCallsDescriptor =
        getByName(ChildDummyService.NUMBER_OF_CHILD_CALLS, snapshot);
    Object numberOfChildCallsValue = snapshot.get(numberOfChildCallsDescriptor);
    AtomicInteger numberOfChildCalls = (AtomicInteger) numberOfChildCallsValue;
    assertEquals(service.getChildCalls().intValue(), numberOfChildCalls.get());
  }

  StatDescriptor getByName(
      String name, ImmutableMap<StatDescriptor, Object> snapshot) {
    for (StatDescriptor key : snapshot.keySet()) {
      if (key.getName().equals(name)) {
        return key;
      }
    }
    throw new RuntimeException(
        "No entry found for " + name + " within " + snapshot);
  }
}
