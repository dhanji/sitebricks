package com.google.sitebricks.http;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This annotation is used to select request handlers based on
 * request parameters. For example, in a single resource URL, you
 * may wish to call different handlers for POST based on the request
 * parameter "action" (action=update, action=delete, etc.). These
 * maybe modeled as form parameters or as part of the query string.
 *
 * <pre>
 *   {@literal @}At("/city/atlantis") {@literal @} Select("action")
 *   public class PictureWebService {
 *
 *     {@literal @}Post("update")
 *     public void update() {
 *       // edit resource in place
 *     }
 *
 *     {@literal @}Post("delete")
 *     public void delete() {
 *       // remove the item...
 *     }
 *   }
 * </pre>
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Select {
  String value();
}
