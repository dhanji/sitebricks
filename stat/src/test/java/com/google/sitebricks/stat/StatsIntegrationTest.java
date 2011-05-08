package com.google.sitebricks.stat;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.sitebricks.stat.testservices.ChildDummyService;
import com.google.sitebricks.stat.testservices.DummyService;
import com.google.sitebricks.stat.testservices.StatExposerTestingService;
import com.google.sitebricks.stat.testservices.StaticDummyService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class StatsIntegrationTest {
  Injector injector;

  @BeforeMethod
  public final void before() {
    injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        install(new StatModule("/stat"));

        bind(DummyService.class);
        bind(ChildDummyService.class);
        bind(StatExposerTestingService.class);
      }
    });
  }

  @Test
  public final void testPublishingStatsInDummyService() {
    DummyService service = injector.getInstance(DummyService.class);

    service.call();
    service.call();
    service.call();

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();

    assertEquals(snapshot.size(), 2);

    // Here we check the value of the field, NUMBER_OF_CALLS
    StatDescriptor numberOfCallsDescriptor =
        getByName(DummyService.NUMBER_OF_CALLS, snapshot);
    String numberOfCallsValue = (String) snapshot.get(numberOfCallsDescriptor);
    assertEquals(String.valueOf(service.getCalls()), numberOfCallsValue);

    // Here we check the value of the method, CALL_LATENCY_NS
    StatDescriptor callLatencyNsDescriptor =
        getByName(DummyService.CALL_LATENCY_NS, snapshot);
    String callLatencyValue = (String) snapshot.get(callLatencyNsDescriptor);
    assertEquals(service.getCallLatencyMs().toString(), callLatencyValue);
  }

  /**
   * This test illustrates how stats are published from parent classes when
   * a child class is published.
   */
  @Test public void testPublishingStatsInChildService() {
    ChildDummyService service = injector.getInstance(ChildDummyService.class);

    service.call();
    service.call();

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();

    // We expect 1 stat from the child class and 2 from its parent
    assertEquals(snapshot.size(), 3);
    StatDescriptor numberOfChildCallsDescriptor =
        getByName(ChildDummyService.NUMBER_OF_CHILD_CALLS, snapshot);
    String numberOfChildCallsValue =
        (String) snapshot.get(numberOfChildCallsDescriptor);
    assertEquals(
        String.valueOf(service.getChildCalls()), numberOfChildCallsValue);

    // Below we check the value of the stats on the parent class
    StatDescriptor numberOfCallsDescriptor =
        getByName(DummyService.NUMBER_OF_CALLS, snapshot);
    String numberOfCallsValue = (String) snapshot.get(numberOfCallsDescriptor);
    assertEquals(String.valueOf(service.getCalls()), numberOfCallsValue);

    StatDescriptor callLatencyNsDescriptor =
        getByName(DummyService.CALL_LATENCY_NS, snapshot);
    String callLatencyValue = (String) snapshot.get(callLatencyNsDescriptor);
    assertEquals(service.getCallLatencyMs().toString(), callLatencyValue);
  }

  @Test
  public void testPublishingStatsAsStaticMember() {
    StaticDummyService.reset();
    StaticDummyService service1 = injector.getInstance(StaticDummyService.class);
    StaticDummyService service2 = injector.getInstance(StaticDummyService.class);

    service1.call();
    service2.call();

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();
    assertEquals(snapshot.size(), 1);
    StatDescriptor staticCallsDescriptor =
        getByName(StaticDummyService.STATIC_CALLS, snapshot);
    String numberOfStaticCallsValue =
        (String) snapshot.get(staticCallsDescriptor);
    assertEquals(
        String.valueOf(StaticDummyService.getNumberOfStaticCalls()),
        numberOfStaticCallsValue);
  }

  @Test
  public final void testPublishingDuplicatedStat() {
    DummyService service1 = injector.getInstance(DummyService.class);
    DummyService service2 = injector.getInstance(DummyService.class);

    service1.call();
    service2.call();

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();

    assertEquals(2, snapshot.size(), snapshot.toString());
    for (Entry<StatDescriptor, Object> entry : snapshot.entrySet()) {
      assertEquals(Stats.DUPLICATED_STAT_VALUE, entry.getValue(),
          "Unexpected value for " + entry.getKey());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public final void testPublishingUsingDifferentExposers() {
    StatExposerTestingService service =
        injector.getInstance(StatExposerTestingService.class);

    service.call();
    service.call();

    Stats stats = injector.getInstance(Stats.class);
    ImmutableMap<StatDescriptor, Object> snapshot = stats.snapshot();
    assertEquals(snapshot.size(), 8, "Snapshot has unexpected size: " + snapshot);

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

  @Test
  public final void testPublishingStandaloneStat() {
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
