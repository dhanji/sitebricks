package com.google.sitebricks.mail;

import com.google.inject.Guice;
import com.google.sitebricks.mail.Mail.Auth;
import org.junit.Test;

import java.util.List;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
public class MailClientIntegrationTest {

  @Test
  public final void edsl() throws InterruptedException {
    Mail mail = Guice.createInjector().getInstance(Mail.class);

    MailClient client = mail.clientOf("imap.gmail.com", 993)
        .connect(Auth.SSL, "telnet.imap@gmail.com", System.getProperty("sitebricks-mail.password"));

    List<String> capabilities = client.capabilities();
    System.out.println("CAPS! " + capabilities);
//    Thread.sleep(5000L);
    client.disconnect();
    

//    client.listFolders();
//    client.list("Mail");
//    client.fetch();
  }
}
