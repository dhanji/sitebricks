package com.google.inject.stat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a variable to mark it as a statistic to be tracked.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Stat {
  /**
   * The name of the stat to track.
   */
  String value();

  /**
   * An optional human-readable description of this stat.
   */
  String description() default "";
}
