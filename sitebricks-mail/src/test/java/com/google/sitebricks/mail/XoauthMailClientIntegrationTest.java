package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Guice;
import com.google.sitebricks.mail.imap.Folder;
import com.google.sitebricks.mail.imap.FolderStatus;
import com.google.sitebricks.mail.imap.Message;
import com.google.sitebricks.mail.oauth.OAuthConfig;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class XoauthMailClientIntegrationTest {
  // Same as CONSUMER_KEY
  public static final String CLIET_ID_PROP = "xoauth.clientId";
  // Same as CONSUMER_SECRET
  public static final String CLIENT_SECRET_PROP = "xoauth.clientSecret";

  static {
    java.util.logging.ConsoleHandler fh = new java.util.logging.ConsoleHandler();
    java.util.logging.Logger.getLogger("").addHandler(fh);
    java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
  }

  public static void main(String... args) throws InterruptedException, ExecutionException {
    Mail mail = Guice.createInjector().getInstance(Mail.class);
    String clientId = System.getProperty(CLIET_ID_PROP);
    String clientSecret = System.getProperty(CLIENT_SECRET_PROP);

    OAuthorize oauthorize = oauthorize(clientId, clientSecret);
    System.out.println("Requesting email via OAuth...");


    final MailClient client = mail.clientOf("imap.gmail.com", 993)
        .prepareOAuth("dhanji@gmail.com", new OAuthConfig(
            oauthorize.accessToken,
            oauthorize.code,
            clientId,
            clientSecret));

    client.connect();

    List<String> capabilities = client.capabilities();
    System.out.println("CAPS: " + capabilities);

    final ListenableFuture<FolderStatus> fStatus =
        client.statusOf("[Gmail]/All Mail");
    ListenableFuture<Folder> future = client.open("[Gmail]/All Mail");
    final Folder allMail = future.get();
    final FolderStatus folderStatus = fStatus.get();
    System.out
        .println("Folder opened: " + allMail.getName() + " with count " + folderStatus.getMessages());

    future.addListener(new Runnable() {
      @Override
      public void run() {
        ListenableFuture<List<Message>> messages = client.fetch(allMail, 80034, 80084);
        try {
          for (Message message : messages.get()) {
            for (Message.BodyPart bodyPart : message.getBodyParts()) {
            }
          }

          System.out.println("Fetched: " + messages.get().size());
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (ExecutionException e) {
          e.printStackTrace();
        }
        client.disconnect();

        System.exit(0);
      }
    }, Executors.newCachedThreadPool());


  }

  private static OAuthorize oauthorize(String clientId, String clientSecret) {

    // Execute out-of-band OAuth steps to obtain an access token.
    OAuthService oAuthService = new ServiceBuilder()
        .provider(GoogleApi.class)
        .apiKey(clientId)
        .apiSecret(clientSecret)
        .scope("https://mail.google.com/")
        .build();

    Token requestToken = oAuthService.getRequestToken();
    System.out.println("Retrieved Request token. Please paste the verifier from this URL:");
    System.out.println("https://www.google.com/accounts/OAuthAuthorizeToken?oauth_token="
        + requestToken.getToken());

    System.out.print("\n> ");
    Scanner in = new Scanner(System.in);

    Verifier verifier = new Verifier(in.nextLine());
    Token accessToken = oAuthService.getAccessToken(requestToken, verifier);
    System.out.println("\nAccess token successfully retrieved: " + accessToken.getToken());

    return new OAuthorize(accessToken.getToken(), accessToken.getSecret());
  }

  private static class OAuthorize {
    private final String accessToken;
    private final String code;

    private OAuthorize(String accessToken, String code) {
      this.accessToken = accessToken;
      this.code = code;
    }
  }
}
