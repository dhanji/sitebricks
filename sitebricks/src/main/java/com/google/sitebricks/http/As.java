package com.google.sitebricks.http;

import com.google.sitebricks.client.Transport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to indicate the transport to marshall an
 * incoming request object as.
 * <pre>
 *   {@literal @}At("/person/image")
 *   public class PictureWebService {
 *
 *     {@literal @}Get
 *     {@literal @}As(Json.class) Reply&lt;Image&gt; getPictureOf({@literal @}As(Json.class) Person person) {
 *       //return image of person...
 *     }
 *   }
 * </pre>
 *
 *
 * Note that you cannot mix the two kinds of negotiation.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface As {
  public abstract Class<? extends Transport> value();
}
