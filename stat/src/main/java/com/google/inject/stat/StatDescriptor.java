package com.google.inject.stat;

import java.lang.reflect.Member;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public final class StatDescriptor {
  private final Object target;
  private final String name;
  private final String description;
  private final Member member;

  public StatDescriptor(
      Object target, String name, String description, Member member) {
    this.target = target;
    this.name = name;
    this.description = description;
    this.member = member;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public Object getTarget() {
    return target;
  }

  public Member getMember() {
    return member;
  }
}
