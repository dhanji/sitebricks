package com.google.sitebricks.mail.oauth;

import com.ning.http.util.Base64;

/**
 * @author ilguzin@gmail.com (Denis A. Ilguzin http://youdev.co
 */
public class Xoauth2Sasl {

  public static final String ctrlA = "\001";

  /**
   * Builds an XOAUTH2 SASL client response.
   * <p/>
   * According to https://developers.google.com/gmail/xoauth2_protocol the SASL XOAUTH2 initial
   * client response has the following format:
   * <p/>
   * {@code base64("user=" {User} "^Aauth=Bearer " {Access Token} "^A^A")}
   * <p/>
   * using the base64 encoding mechanism defined in RFC 4648. ^A represents a Control+A (\001).
   *
   * @return A base-64 encoded string containing the auth string suitable for login via xoauth2.
   */
  public static String build(String user, String accessToken) {
    StringBuilder authString =
      new StringBuilder()
        .append("user=").append(user)
        .append(ctrlA).append("auth=Bearer ")
        .append(accessToken).append(ctrlA).append(ctrlA);

    return Base64.encode(authString.toString().getBytes());
  }
}
