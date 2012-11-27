package com.google.sitebricks.client;

import com.google.inject.ImplementedBy;
import com.google.inject.TypeLiteral;

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

    <T> ReadAsBuilder<T> transports(TypeLiteral<T> clazz);

    <T> WebClient<T> transportsText();

    FormatBuilder auth(Auth auth, String username, String password);
  }

  static interface ReadAsBuilder<T> {
    WebClient<T> over(Class<? extends Transport> clazz);
  }
}
