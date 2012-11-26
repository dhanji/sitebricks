package com.google.sitebricks.persist.redis;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@BindingAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface StoreOne {
}
