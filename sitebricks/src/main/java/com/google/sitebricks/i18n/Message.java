package com.google.sitebricks.i18n;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate localization interface methods with this to describe the purpose and value of
 * an internationalized message.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Message {
  String message();
  String description() default "";
}
