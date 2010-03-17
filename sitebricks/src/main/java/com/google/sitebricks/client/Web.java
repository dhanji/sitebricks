package com.google.sitebricks.client;

import com.google.inject.ImplementedBy;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(CommonsWeb.class)
public interface Web {
  enum Auth {
    BASIC, DIGEST
  }

  FormatBuilder clientOf(String url);

  FormatBuilder clientOf(String url, Map<String, String> headers);

  static interface FormatBuilder {
    <T> ReadAsBuilder<T> transports(Class<T> clazz);

    FormatBuilder auth(Auth auth, String username, String password);
  }

  static interface ReadAsBuilder<T> {
    WebClient<T> over(Class<? extends Transport> clazz);
  }
}
