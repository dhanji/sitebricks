package com.google.sitebricks.mail;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.mail.oauth.OAuthConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author dhanji@gmail.com (Dhanji R. Prasanna)
 */
@ImplementedBy(SitebricksMail.class)
public interface Mail {
  AuthBuilder clientOf(String host, int port);

  public enum Auth { PLAIN, SSL, OAUTH }

  public static interface AuthBuilder {
    AuthBuilder timeout(long amount, TimeUnit unit);

    AuthBuilder executors(ExecutorService bossPool, ExecutorService workerPool);

    MailClient prepare(Auth authType, String username, String password);


    MailClient prepareOAuth(String username, OAuthConfig config);
  }
}
