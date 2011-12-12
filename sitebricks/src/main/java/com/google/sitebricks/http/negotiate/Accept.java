package com.google.sitebricks.http.negotiate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to select request handlers based on
 * request headers provided by clients and can be used to perform HTTP
 * content negotiation. If a client sends in a request for an
 * image via uri, "/city/atlantis" and accepts JPEG type, then
 * you may instruct sitebricks to choose a request handler as
 * follows:
 * <pre>
 *   {@literal @}At("/city/atlantis")
 *   public class PictureWebService {
 *
 *     {@literal @}Accept("Accept") @Get("image/jpeg")
 *     public Response getJpeg() {
 *       //return JPEG image...
 *     }
 *
 *     {@literal @}Accept("Accept") @Get("image/png")
 *     public Response getPng() {
 *       //return PNG image instead...
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
@Target(ElementType.METHOD)
public @interface Accept {
  public abstract String value();
}
