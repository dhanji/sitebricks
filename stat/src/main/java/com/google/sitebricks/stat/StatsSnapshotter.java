package com.google.sitebricks.stat;

import com.google.common.collect.ImmutableMap;
import com.google.inject.ImplementedBy;

/**
 * This API represents the collection of registered stats within an
 * injector. Its main roles are to act as a container for these stats, and
 * to provide access to them through its {@link #snapshot()} method.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@ImplementedBy(Stats.class)
public interface StatsSnapshotter {
  ImmutableMap<StatDescriptor, Object> snapshot();
}
