package com.google.inject.stat;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.stat.testservices.ChildDummyService;
import com.google.inject.stat.testservices.DummyService;
import com.google.inject.stat.testservices.StatExposerTestingService;
import com.google.inject.stat.testservices.StaticDummyService;

import org.junit.Test;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class StatsIntegrationTest {

  Injector injector = Guice.createInjector(new AbstractModule() {
    @Override protected void configure() {
      install(new StatModule("/stat"));

      bind(DummyService.class);
      bind(ChildDummyService.class);
      bind(StatExposerTestingService.class);
    }
  });

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

    // We expect a string representation of the value within the snapshot.
    String numberOfCallsValue = (String) snapshot.get(numberOfCallsDescriptor);
    assertEquals(String.valueOf(service.getCalls()), numberOfCallsValue);

    // Here we check the value of the method, CALL_LATENCY_NS
    StatDescriptor callLatencyNsDescriptor =
        getByName(DummyService.CALL_LATENCY_NS, snapshot);
    String callLatencyNsValue = (String) snapshot.get(callLatencyNsDescriptor);
    assertEquals(
        String.valueOf(service.getCallLatencyMs()), callLatencyNsValue);
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
    String numberOfChildCallsValue =
        (String) snapshot.get(numberOfChildCallsDescriptor);
    assertEquals(
        String.valueOf(service.getChildCalls()), numberOfChildCallsValue);
  }

  @Test public void testPublishingStatsAsStaticMember() {
    StaticDummyService service1 = injector.getInstance(StaticDummyService.class);
    StaticDummyService service2 = injector.getInstance(StaticDummyService.class);

    service1.call();
    service2.call();

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();
    assertEquals(1, snapshot.size());
    StatDescriptor staticCallsDescriptor =
        getByName(StaticDummyService.STATIC_CALLS, snapshot);
    String numberOfStaticCallsValue =
        (String) snapshot.get(staticCallsDescriptor);
    assertEquals(
        String.valueOf(StaticDummyService.getNumberOfStaticCalls()),
        numberOfStaticCallsValue);
  }

  @Test public final void testPublishingDuplicatedStat() {
    DummyService service1 = injector.getInstance(DummyService.class);
    DummyService service2 = injector.getInstance(DummyService.class);

    service1.call();
    service2.call();

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();

    assertEquals(snapshot.toString(), 2, snapshot.size());
    for (Entry<StatDescriptor, Object> entry : snapshot.entrySet()) {
      assertEquals(
          "Unexpected value for " + entry.getKey(),
          Stats.DUPLICATED_STAT_VALUE, entry.getValue());
    }
  }

  @SuppressWarnings("unchecked")
  @Test public final void testPublishingUsingDifferentExposers() {
    StatExposerTestingService service =
        injector.getInstance(StatExposerTestingService.class);

    service.call();
    service.call();

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();
    assertEquals(
        "Snapshot has unexpected size: " + snapshot, 8, snapshot.size());

    AtomicInteger atomicIntegerCallCount = service.getCallCount();
    String stringCallCount = String.valueOf(atomicIntegerCallCount);
    
    StatDescriptor callsDefaultExposerDescriptor = getByName(
        StatExposerTestingService.CALLS_WITH_DEFAULT_EXPOSER, snapshot);
    String callsDefaultExposerValue = 
        (String) snapshot.get(callsDefaultExposerDescriptor);
    assertEquals(stringCallCount, callsDefaultExposerValue);

    StatDescriptor callsIdentityExposerDescriptor = getByName(
        StatExposerTestingService.CALLS_WITH_IDENTITY_EXPOSER, snapshot);
    AtomicInteger callsIdentityExposerValue = 
        (AtomicInteger) snapshot.get(callsIdentityExposerDescriptor);
    assertEquals(atomicIntegerCallCount.get(), callsIdentityExposerValue.get());
    
    StatDescriptor callsInferenceExposerDescriptor = getByName(
        StatExposerTestingService.CALLS_WITH_INFERENCE_EXPOSER, snapshot);
    String callsInferenceExposerValue = 
        (String) snapshot.get(callsInferenceExposerDescriptor);
    assertEquals(stringCallCount, callsInferenceExposerValue);

    StatDescriptor callsToStringExposerDescriptor = getByName(
        StatExposerTestingService.CALLS_WITH_TO_STRING_EXPOSER, snapshot);
    String callsToStringExposerValue = 
        (String) snapshot.get(callsToStringExposerDescriptor);
    assertEquals(stringCallCount, callsToStringExposerValue);

    List<Integer> callsList = service.getCallsList();
    String callsListAsString = String.valueOf(callsList);

    StatDescriptor listDefaultExposerDescriptor = getByName(
        StatExposerTestingService.LIST_WITH_DEFAULT_EXPOSER, snapshot);
    List<Integer> listDefaultExposerValue =
        (List<Integer>) snapshot.get(listDefaultExposerDescriptor);
    assertEquals(callsList, listDefaultExposerValue);

    StatDescriptor listIdentityExposerDescriptor = getByName(
        StatExposerTestingService.LIST_WITH_IDENTITY_EXPOSER, snapshot);
    List<Integer> listIdentityExposerValue =
        (List<Integer>) snapshot.get(listIdentityExposerDescriptor);
    assertEquals(callsList, listIdentityExposerValue);
    
    StatDescriptor listInferenceExposerDescriptor = getByName(
        StatExposerTestingService.LIST_WITH_INFERENCE_EXPOSER, snapshot);
    List<Integer> listInferenceExposerValue =
        (List<Integer>) snapshot.get(listInferenceExposerDescriptor);
    assertEquals(callsList, listInferenceExposerValue);

    StatDescriptor listToStringExposerDescriptor = getByName(
        StatExposerTestingService.LIST_WITH_TO_STRING_EXPOSER, snapshot);
    String listToStringExposerValue =
        (String) snapshot.get(listToStringExposerDescriptor);
    assertEquals(callsListAsString, listToStringExposerValue);
  }

  @Test public final void testPublishingStandaloneStat() {
    StatRegistrar statRegistrar = injector.getInstance(StatRegistrar.class);

    AtomicInteger statValue = new AtomicInteger(0);
    statRegistrar.registerSingleStat("single-stat", "", statValue);

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();

    StatDescriptor numberOfChildCallsDescriptor =
        getByName("single-stat", snapshot);
    String snapshottedValue =
        (String) snapshot.get(numberOfChildCallsDescriptor);
    assertEquals(String.valueOf(statValue.intValue()), snapshottedValue);
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
