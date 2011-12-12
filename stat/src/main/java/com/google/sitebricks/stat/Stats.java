package com.google.sitebricks.stat;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * This class represents the collection of registered stats within an
 * application.  Its main roles are to act as a container for these stats, and
 * to provide access to them through its {@link #snapshot()} method.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
final class Stats {
  private static final Logger logger =
      Logger.getLogger(Stats.class.getCanonicalName());

  /** This is the value used for duplicate stats. */
  static final String DUPLICATED_STAT_VALUE = "duplicated value";

  private final ConcurrentMap<String, StatDescriptor> stats =
      new MapMaker().makeMap();

  private Injector injector;

  @Inject Stats() { }

  @SuppressWarnings("UnusedDeclaration")
  @Inject void setInjector(Injector injector) {
    this.injector = injector;
    checkBindingsExistForExposers();
  }

  void register(StatDescriptor statDescriptor) {
    String statName = statDescriptor.getName();
    StatDescriptor existingDescriptor =
        stats.putIfAbsent(statName, statDescriptor);

    if (existingDescriptor != null &&
        !(existingDescriptor.getStatReader().equals(
            statDescriptor.getStatReader()))) {
      logger.warning(String.format(
          "You have two non-static stats using the same name [%s], "
              + "this is not allowed. \n"
              + "First encounter:  %s\nSecond encounter: %s",
          statName, existingDescriptor, statDescriptor));

      StatDescriptor syntheticDescriptor = StatDescriptor.of(
          statName, "Placeholder for duplicate stat",
          StatReaders.forObject(DUPLICATED_STAT_VALUE),
          StatExposers.IdentityExposer.class);
      stats.put(statName, syntheticDescriptor);
    } else {
      if (injector != null) {
        injector.getBinding(statDescriptor.getStatExposerClass());
      }
      stats.put(statName, statDescriptor);
    }
  }

  ImmutableMap<StatDescriptor, Object> snapshot() {
    checkState(injector != null,
        "Stats may not be snapshotted yet; injector has not been set");
    ImmutableMap.Builder<StatDescriptor, Object> builder =
        ImmutableMap.builder();
    for (StatDescriptor statDescriptor : stats.values()) {
      // Here we read the raw value
      Object statValue = statDescriptor.getStatReader().readStat();

      // And here we are careful to expose only the reference we should
      StatExposer statExposer =
          getStatExposer(statDescriptor.getStatExposerClass());
      @SuppressWarnings("unchecked") // We know we don't guarantee a <T> here.
      Object exposedValue = statExposer.expose(statValue);
      builder.put(statDescriptor, exposedValue);
    }
    return builder.build();
  }

  private StatExposer getStatExposer(
      Class<? extends StatExposer> statExposerClass) {
    return injector.getInstance(statExposerClass);
  }

  private void checkBindingsExistForExposers() {
    // We do an up-front check of
    Set<Class<? extends StatExposer>> statExposerClasses = Sets.newHashSet();
    for (StatDescriptor statDescriptor : stats.values()) {
      statExposerClasses.add(statDescriptor.getStatExposerClass());
    }
    for (Class<? extends StatExposer> statExposerClass : statExposerClasses) {
      injector.getBinding(statExposerClass);
    }
  }
}
