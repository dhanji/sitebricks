package com.google.sitebricks.mail;

import com.google.inject.Guice;

import java.util.concurrent.ExecutionException;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MailClientFailedAuthIntegrationTest {
  public static void main(String...args) throws InterruptedException, ExecutionException {
    Mail mail = Guice.createInjector().getInstance(Mail.class);

    final MailClient client = mail.clientOf("imap.gmail.com", 993)
        .prepare(Mail.Auth.SSL, "telnet.imap@gmail.com", System.getProperty("sitebricks-mail.password"));

    if (!client.connect()) {
      System.out.println("Authentication failed due to " + client.lastError());
    } else
      System.out.println("Auth success");

    System.exit(0);
  }
}
