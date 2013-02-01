package com.google.sitebricks.i18n;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotate localization interface methods with this to describe the purpose and value of
 * an internationalized message.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Message {
  String message();
  String description() default "";
}
