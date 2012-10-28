package com.google.sitebricks.channel;

import com.google.sitebricks.client.Transport;
import com.google.sitebricks.client.transport.Text;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotate a method with {@literal @}Observe to enable it to receive
 * messages over a channel.
 *
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Observe {
  Class<? extends Transport> value() default Text.class;
}
