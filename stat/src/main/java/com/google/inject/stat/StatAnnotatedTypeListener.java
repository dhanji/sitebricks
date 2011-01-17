package com.google.inject.stat;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
class StatAnnotatedTypeListener implements TypeListener {

  // TODO(ffaber): add logic to climb the class hierarchy and pull in members
  // of super types.

  private final Map<Class<?>, List<StatAnnotatedMember>> classToMembersCache =
      new MapMaker().weakKeys().makeComputingMap(
          new Function<Class<?>, List<StatAnnotatedMember>>() {
            @Override public List<StatAnnotatedMember> apply(Class<?> clazz) {
              List<StatAnnotatedMember> annotatedMembers = Lists.newArrayList();

              for (Method method : clazz.getDeclaredMethods()) {
                Stat stat = method.getAnnotation(Stat.class);
                if (stat != null) {
                  annotatedMembers.add(new StatAnnotatedMember(stat, method));
                }
              }
              for (Field field : clazz.getDeclaredFields()) {
                Stat stat = field.getAnnotation(Stat.class);
                if (stat != null) {
                  annotatedMembers.add(new StatAnnotatedMember(stat, field));
                }
              }

              return annotatedMembers;
            }
          });

  private final Stats stats;

  StatAnnotatedTypeListener(Stats stats) {
    this.stats = stats;
  }

  @Override
  public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
    List<StatAnnotatedMember> targetsOnClass =
        classToMembersCache.get(type.getRawType());
    for (StatAnnotatedMember statAnnotatedMember : targetsOnClass) {
      final Stat stat = statAnnotatedMember.getStat();
      final Member member = statAnnotatedMember.getMember();
      encounter.register(new InjectionListener<I>() {
        @Override
        public void afterInjection(I injectee) {
          stats.register(new StatDescriptor(
              injectee, stat.value(), stat.description(), member));
        }
      });
    }
  }
}
