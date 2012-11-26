package com.google.sitebricks.persist;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
  /**
   * Datastore selector. Define the datastore by creating your own annotation
   * and providing it to the appropriate Sitebricks persistence module.
   */
  Class<? extends Annotation> value() default Transactional.class;
}
