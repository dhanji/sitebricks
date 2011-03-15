package com.google.sitebricks.rendering;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the page is to be rendered and its output inserted into
 * a decorator page using the @Decorate template annotation.  
 * 
 * @author John Patterson (jdpatterson@gmail.com)
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Decorated {
}
