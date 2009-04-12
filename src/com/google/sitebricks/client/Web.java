package com.google.sitebricks.client;

import com.google.inject.ImplementedBy;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(CommonsWeb.class)
public interface Web {

    public FormatBuilder clientOf(String url);

    public FormatBuilder clientOf(String url, Map<String, String> headers);

    public static interface FormatBuilder {
        <T> ReadAsBuilder<T> transports(Class<T> clazz);
    }

    public static interface ReadAsBuilder<T> {
        WebClient<T> over(Class<? extends Transport> clazz);
    }
}
