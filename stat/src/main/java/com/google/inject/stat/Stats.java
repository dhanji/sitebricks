package com.google.inject.stat;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class Stats {
  private final Map<String, StatDescriptor> stats = Maps.newLinkedHashMap();

  void register(StatDescriptor statDescriptor) {
    StatDescriptor first = stats.get(statDescriptor.getStat());
    if (null != first) {
      throw new IllegalArgumentException(String.format(
          "You have two stats using the same name [%s] in different types, this is not allowed. \n"
          + "First encounter: %s\nSecond encounter: %s", statDescriptor.getStat(), first.getStat(),
          statDescriptor.getField().getDeclaringClass()));
    }

    stats.put(statDescriptor.getStat(), statDescriptor);
  }

  public Map<String, String> snapshot() {
    Map<String, String> snapshot = Maps.newHashMap();
    // Iterates all stats and produces a current snapshot.
    for (StatDescriptor stat : stats.values()) {
      snapshot.put(stat.getStat(), stat.read());
    }

    return snapshot;
  }
}
