package com.google.sitebricks.options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation marker that identifies a type as a repository
 * of options (example, command line options).
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Options {
  String value() default "";
}
