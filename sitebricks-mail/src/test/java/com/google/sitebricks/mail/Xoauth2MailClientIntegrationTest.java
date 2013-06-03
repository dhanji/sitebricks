package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Guice;
import com.google.sitebricks.mail.imap.Folder;
import com.google.sitebricks.mail.imap.FolderStatus;
import com.google.sitebricks.mail.imap.Message;
import com.google.sitebricks.mail.oauth.OAuth2Config;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * @author ilguzin@gmail.com (Denis A. Ilguzin http://youdev.co)
 */
public class Xoauth2MailClientIntegrationTest {

  // ACCESS_TOKEN
  public static final String ACCESS_TOKEN = "xoauth2.accessToken";

  static {
    java.util.logging.ConsoleHandler fh = new java.util.logging.ConsoleHandler();
    java.util.logging.Logger.getLogger("").addHandler(fh);
    java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
  }

  public static void main(String... args) throws InterruptedException, ExecutionException {
    Mail mail = Guice.createInjector().getInstance(Mail.class);

    System.out.println("Requesting email via OAuth2 authorization...");


    final MailClient client = mail.clientOf("imap.gmail.com", 993)
        .prepareOAuth2("ilguzin@gmail.com", new OAuth2Config(ACCESS_TOKEN, "empty_key", "empty_key"));

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

}
