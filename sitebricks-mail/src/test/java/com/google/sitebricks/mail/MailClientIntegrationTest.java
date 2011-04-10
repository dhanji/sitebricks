package com.google.sitebricks.mail;

import com.google.inject.Guice;
import com.google.sitebricks.mail.Mail.Auth;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MailClientIntegrationTest {
  public static void main(String...args) throws InterruptedException, ExecutionException {
    Mail mail = Guice.createInjector().getInstance(Mail.class);

    MailClient client = mail.clientOf("imap.gmail.com", 993)
        .connect(Auth.SSL, "telnet.imap@gmail.com", System.getProperty("sitebricks-mail.password"));

    List<String> capabilities = client.capabilities();
    System.out.println("CAPS: " + capabilities);

    client.listFolders();
    System.out.println("Folders retrieved were: ");

    Thread.sleep(100000L);
    client.disconnect();
  }
}
