/**
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.google.inject.stat;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * A {@link StatCollector} performs the work of scanning the members of a class
 * to collect each member annotated with {@link Stat}.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
// TODO(ffaber): add logic to climb the class hierarchy and pull in members
// of super types.
class StatCollector
    implements Function<Class<?>, List<MemberAnnotatedWithAtStat>> {

  enum StaticMemberPolicy {
    INCLUDE_STATIC_MEMBERS {
      @Override boolean shouldAccept(Member member) {
        return isStaticMember(member);
      }
    },
    EXCLUDE_STATIC_MEMBERS {
      @Override boolean shouldAccept(Member member) {
        return !isStaticMember(member);
      }
    };

    abstract boolean shouldAccept(Member member);

    private static boolean isStaticMember(Member member) {
      return (member.getModifiers() & Modifier.STATIC) != 0;
    }
  }

  private final StaticMemberPolicy staticMemberPolicy;

  StatCollector(StaticMemberPolicy staticMemberPolicy) {
    this.staticMemberPolicy = staticMemberPolicy;
  }

  @Override public List<MemberAnnotatedWithAtStat> apply(Class<?> clazz) {
    List<MemberAnnotatedWithAtStat> annotatedMembers = Lists.newArrayList();

    for (Method method : clazz.getDeclaredMethods()) {
      Stat stat = method.getAnnotation(Stat.class);
      if (stat != null && staticMemberPolicy.shouldAccept(method)) {
        annotatedMembers.add(new MemberAnnotatedWithAtStat(stat, method));
      }
    }
    for (Field field : clazz.getDeclaredFields()) {
      Stat stat = field.getAnnotation(Stat.class);
      if (stat != null && staticMemberPolicy.shouldAccept(field)) {
        annotatedMembers.add(new MemberAnnotatedWithAtStat(stat, field));
      }
    }

    return annotatedMembers;
  }
}
