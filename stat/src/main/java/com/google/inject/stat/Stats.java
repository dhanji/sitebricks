package com.google.inject.stat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class Stats {
  private final ConcurrentMap<String, StatDescriptor> stats =
      new MapMaker().makeMap();

  private static final Logger logger =
      Logger.getLogger(Stats.class.getCanonicalName());

  /** This is the value used for duplicate stats. */
  static final String DUPLICATED_STAT_VALUE = "duplicated value";

  void register(StatDescriptor statDescriptor) {
    String statName = statDescriptor.getName();
    StatDescriptor existingDescriptor =
        stats.putIfAbsent(statName, statDescriptor);

    if (existingDescriptor != null &&
        !isAnAllowedDuplicate(statDescriptor, existingDescriptor)) {
      logger.warning(String.format(
          "You have two non-static stats using the same name [%s], "
              + "this is not allowed. \n"
              + "First encounter:  %s\nSecond encounter: %s",
          statName,
          existingDescriptor.getTarget(),
          statDescriptor.getTarget()));

      ConstantStatContainer constantStatContainer =
          new ConstantStatContainer(DUPLICATED_STAT_VALUE);
      StatDescriptor syntheticDescriptor = StatDescriptor.of(
          constantStatContainer, statName, "",
          ConstantStatContainer.getMember());
      stats.put(statName, syntheticDescriptor);
    } else {
      stats.put(statName, statDescriptor);
    }
  }

  /**
   * This method returns true if the {@code newDescriptor} is an <em>allowed
   * duplicate</em> of the given {@code existingDescriptor}.  The only reason
   * this might be true is if both represent a descriptor on a registered
   * static member on the same class.
   */
  boolean isAnAllowedDuplicate(
      StatDescriptor newDescriptor, StatDescriptor existingDescriptor) {
    Member newMember = newDescriptor.getMember();
    Member existingMember = existingDescriptor.getMember();
    // If they're unequal, then they're clearly not representative of the same
    // member.
    if (!newMember.equals(existingMember)) {
      return false;
    }

    // If this is not a static member, then we return false as not to allow
    // published instance members to clobber each other.
    if ((existingMember.getModifiers() & Modifier.STATIC) == 0) {
      return false;
    }

    return true;
  }

  ImmutableMap<StatDescriptor, Object> snapshot() {
    ImmutableMap.Builder<StatDescriptor, Object> builder =
        ImmutableMap.builder();
    for (StatDescriptor statDescriptor : stats.values()) {
      builder.put(statDescriptor, read(statDescriptor));
    }
    return builder.build();
  }

  Object read(StatDescriptor statDescriptor) {
    Object target = statDescriptor.getTarget();

    Member member = statDescriptor.getMember();
    if (member instanceof Field) {
      Field field = (Field) member;
      if (!field.isAccessible()) {
        field.setAccessible(true);
      }
      try {
        return field.get(target);
      } catch (IllegalAccessException e) {
        return "unable to read: " + e.getMessage();
      }
    }

    if (member instanceof Method) {
      Method method = (Method) member;
      if (!method.isAccessible()) {
        method.setAccessible(true);
      }
      try {
        return method.invoke(target);
      } catch (InvocationTargetException e) {
        return "unable to read: " + e.getMessage();
      } catch (IllegalAccessException e) {
        return "unable to read: " + e.getMessage();
      }
    }

    throw new IllegalArgumentException(
        "Unexpected member type on descriptor: " + statDescriptor);
  }

  /** This class is useful to publish a stat that has a constant string value. */
  static final class ConstantStatContainer {
    final String staticValue;

    ConstantStatContainer(String staticValue) {
      this.staticValue = staticValue;
    }

    static Member getMember() {
      try {
        return ConstantStatContainer.class.getDeclaredField("staticValue");
      } catch (NoSuchFieldException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
