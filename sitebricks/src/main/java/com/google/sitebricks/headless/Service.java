package com.google.sitebricks.headless;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Analogous to {@literal @}Show but representing a headless (page-less)
 * web service. Sometimes called a Restful web service. This kind of
 * web page has no corresponding template file and the page class returns
 * a response directly.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Service {
}
