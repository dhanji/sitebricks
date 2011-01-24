package com.google.inject.stat;

import com.google.inject.stat.StatExposers.InferenceExposer;

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
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Stat {

  /** The name of the stat to track. */
  String value();

  /** An optional human-readable description of this stat. */
  String description() default "";

  /**
   * Class of the exposer to apply before exposing a reference to the stat
   * value.
   */
  Class<? extends StatExposer> exposer() default InferenceExposer.class;
}
