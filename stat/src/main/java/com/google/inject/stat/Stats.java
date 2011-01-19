package com.google.inject.stat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapMaker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class Stats {
  private final Map<String, StatDescriptor> stats = new MapMaker().makeMap();

  void register(StatDescriptor statDescriptor) {
    StatDescriptor existingDescriptor = stats.get(statDescriptor.getName());

    if (existingDescriptor != null &&
        !isAnAllowedDuplicate(statDescriptor, existingDescriptor)) {
      throw new IllegalArgumentException(String.format(
          "You have two stats using the same name [%s] in different types, "
              + "this is not allowed. \n"
              + "First encounter:  %s\nSecond encounter: %s",
          statDescriptor.getName(),
          existingDescriptor.getMember().getDeclaringClass(),
          statDescriptor.getMember().getDeclaringClass()));
    }

    stats.put(statDescriptor.getName(), statDescriptor);
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
}
