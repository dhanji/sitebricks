package com.google.inject.stat;

import com.google.common.base.Objects;

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

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("name", name)
        .add("description", description)
        .add("target", target)
        .add("member", member)
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StatDescriptor)) {
      return false;
    }

    StatDescriptor that = (StatDescriptor) o;
    return Objects.equal(this.name, that.name)
        && Objects.equal(this.description, that.description)
        && Objects.equal(this.member, that.member)
        && Objects.equal(this.target, that.target)
        && Objects.equal(this.member, that.member);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, description, member, target);
  }
}
