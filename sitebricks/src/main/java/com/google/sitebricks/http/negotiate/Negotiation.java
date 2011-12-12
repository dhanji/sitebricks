package com.google.sitebricks.http.negotiate;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A simple binding annotation to share negotiation services.
 * 
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface Negotiation {
}
