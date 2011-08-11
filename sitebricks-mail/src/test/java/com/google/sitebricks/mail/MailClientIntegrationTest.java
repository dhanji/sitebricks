package com.google.sitebricks.mail;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Guice;
import com.google.sitebricks.mail.Mail.Auth;
import com.google.sitebricks.mail.imap.Folder;
import com.google.sitebricks.mail.imap.MessageStatus;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MailClientIntegrationTest {
  public static void main(String...args) throws InterruptedException, ExecutionException {
    Mail mail = Guice.createInjector().getInstance(Mail.class);

    final MailClient client = mail.clientOf("imap.gmail.com", 993)
        .connect(Auth.SSL, "telnet.imap@gmail.com", System.getProperty("sitebricks-mail.password"));

    List<String> capabilities = client.capabilities();
    System.out.println("CAPS: " + capabilities);

    client.statusOf("[Gmail]/All Mail");
    ListenableFuture<Folder> future = client.open("[Gmail]/All Mail");
    final Folder allMail = future.get();
    System.out.println("Folder opened: " + allMail.getName() + " with count " + allMail.getCount());

    future.addListener(new Runnable() {
      @Override
      public void run() {
//        client.watch(allMail, new FolderObserver() {
//          @Override
//          public void onMailAdded() {
//            System.out.println("New mail arrived!!");
//          }
//
//          @Override
//          public void onMailRemoved() {
//            System.out.println("Old mail removed!!");
//          }
//        });

        ListenableFuture<List<MessageStatus>> messages = client.list(allMail, 1, 4);
//        ListenableFuture<List<Message>> messages = client.fetch(allMail, 1, 9);
        try {
//          for (Message message : messages.get()) {
//            System.out.println(ToStringBuilder.reflectionToString(message));
//            for (Message.BodyPart bodyPart : message.getBodyParts()) {
//              System.out.println(ToStringBuilder.reflectionToString(bodyPart));
//            }
//          }
          for (MessageStatus message : messages.get()) {
            System.out.println(ToStringBuilder.reflectionToString(message));
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
