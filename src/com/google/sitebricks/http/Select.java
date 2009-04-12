package com.google.sitebricks.http;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to select request handlers based on
 * specified request headers. It can be used to perform HTTP
 * content negotiation. If a client sends in a request for an
 * image via uri, "/city/atlantis" and accepts JPEG type, then
 * you may instruct sitebricks to choose a request handler as
 * follows:
 * <pre>
 *   {@literal @}At("/city/atlantis")
 *   public class PictureWebService {
 *
 *     {@literal @}Select("Accept") @Get("image/jpeg")
 *     public Response getJpeg() {
 *       //return JPEG image...
 *     }
 *
 *     {@literal @}Select("Accept") @Get("image/png")
 *     public Response getPng() {
 *       //return PNG image instead...
 *     }
 *   }
 *
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.METHOD)
public @interface Select {
    String value();
}
