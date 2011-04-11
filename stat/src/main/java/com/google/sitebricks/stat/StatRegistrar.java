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

package com.google.sitebricks.stat;

import com.google.common.collect.MapMaker;

import java.lang.reflect.Member;
import java.util.List;
import java.util.Map;

/**
 * A {@link StatRegistrar} offers the means by which to register a stat.
 *
 * @author ffaber@gmail.com (Fred Faber)
 */
public final class StatRegistrar {
  /**
   * The default exposer to use, which is suitable in most cases.
   * Note that this would be best defined within {@link Stat}, but
   * static fields declared within {@code @interface} definitions lead to
   * javac bugs, such as is described here:
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=324931
   */
  Class<? extends StatExposer> DEFAULT_EXPOSER_CLASS = StatExposers.InferenceExposer.class;

  private final Map<Class<?>, List<MemberAnnotatedWithAtStat>>
      classesToInstanceMembers =
      new MapMaker().weakKeys().makeComputingMap(
          new StatCollector(
              StatCollector.StaticMemberPolicy.EXCLUDE_STATIC_MEMBERS));

  private final Map<Class<?>, List<MemberAnnotatedWithAtStat>>
      classesToStaticMembers =
      new MapMaker().weakKeys().makeComputingMap(
          new StatCollector(
              StatCollector.StaticMemberPolicy.INCLUDE_STATIC_MEMBERS));

  private final Stats stats;

  StatRegistrar(Stats stats) {
    this.stats = stats;
  }

  public void registerSingleStat(String name, String description, Object stat) {
    registerSingleStat(
        name, description, StatReaders.forObject(stat), DEFAULT_EXPOSER_CLASS);
  }

  public void registerSingleStat(
      String name, String description, StatReader statReader,
      Class<? extends StatExposer> statExposerClass) {
    stats.register(
        StatDescriptor.of(name, description, statReader, statExposerClass));
  }

  public void registerStaticStatsOn(Class<?> clazz) {
    List<MemberAnnotatedWithAtStat> annotatedMembers =
        classesToStaticMembers.get(clazz);
    for (MemberAnnotatedWithAtStat annotatedMember : annotatedMembers) {
      Stat stat = annotatedMember.getStat();
      stats.register(StatDescriptor.of(
          stat.value(),
          stat.description(),
          StatReaders.forStaticMember(annotatedMember.<Member>getMember()),
          stat.exposer()));
    }
  }

  public void registerAllStatsOn(Object target) {
    List<MemberAnnotatedWithAtStat> annotatedMembers =
        classesToInstanceMembers.get(target.getClass());
    for (MemberAnnotatedWithAtStat annotatedMember : annotatedMembers) {
      Stat stat = annotatedMember.getStat();
      stats.register(StatDescriptor.of(
          stat.value(),
          stat.description(),
          StatReaders.forMember(annotatedMember.<Member>getMember(), target),
          stat.exposer()));
    }
    registerStaticStatsOn(target.getClass());
  }
}
