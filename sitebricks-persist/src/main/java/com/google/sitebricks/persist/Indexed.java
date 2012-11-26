package com.google.sitebricks.persist;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Indexed {
  /**
   * The value represents the name of the underlying index.
   * This is only used by some datastores and should generally
   * be left blank for default behavior.
   */
  String value() default "";
}
