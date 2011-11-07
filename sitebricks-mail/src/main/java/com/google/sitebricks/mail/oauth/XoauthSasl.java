package com.google.sitebricks.mail.oauth;

import com.ning.http.util.Base64;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class XoauthSasl {
  private final OAuthConsumer consumer;
  private final String email;

  public XoauthSasl(String email, String consumerKey, String consumerSecret) {
    this.email = email;
    this.consumer = new OAuthConsumer(null, consumerKey, consumerSecret, null);
  }

  /**
   * Builds an XOAUTH SASL client response.
   *
   * @return A base-64 encoded containing the auth string suitable for login via xoauth.
   */
  public String build(Protocol protocol, String oauthToken, String oauthTokenSecret)
      throws IOException, OAuthException, URISyntaxException {
    OAuthAccessor accessor = new OAuthAccessor(consumer);
    accessor.tokenSecret = oauthTokenSecret;

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(OAuth.OAUTH_SIGNATURE_METHOD, "HMAC-SHA1");
    parameters.put(OAuth.OAUTH_TOKEN, oauthToken);

    String url = String.format("https://mail.google.com/mail/b/%s/%s/", email,
        (Protocol.IMAP == protocol) ? "imap" : "smtp");

    OAuthMessage message = new OAuthMessage(
        "GET",
        url,
        parameters.entrySet());
    message.addRequiredParameters(accessor);

    StringBuilder authString = new StringBuilder();
    authString.append("GET ");
    authString.append(url);
    authString.append(" ");
    int i = 0;
    for (Map.Entry<String, String> entry : message.getParameters()) {
      if (i++ > 0) {
        authString.append(",");
      }
      authString.append(OAuth.percentEncode(entry.getKey()));
      authString.append("=\"");
      authString.append(OAuth.percentEncode(entry.getValue()));
      authString.append("\"");
    }

    return Base64.encode(authString.toString().getBytes());
  }
}
