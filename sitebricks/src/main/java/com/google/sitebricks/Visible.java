// Copyright 2009. Google, Inc. All Rights Reserved.
package com.google.sitebricks;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotate a field with this marker to denote it
 * visible in templates. By defauly a property is
 * read/write. In other words, request parameters
 * matching the field name will be bound to the
 * field. Setting {@code readOnly} to true will
 * prevent this from happening and only expose the
 * field to be read into templates.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Visible {
  boolean readOnly() default false;
}
