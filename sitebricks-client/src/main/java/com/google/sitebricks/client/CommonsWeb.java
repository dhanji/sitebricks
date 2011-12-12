package com.google.sitebricks.client;

import com.google.inject.Inject;
import com.google.inject.Provider;
import net.jcip.annotations.Immutable;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
*/
@Immutable
class CommonsWeb implements Web {
    private final Provider<WebClientBuilder> builder;

    @Inject
    public CommonsWeb(Provider<WebClientBuilder> builder) {
        this.builder = builder;
    }

    public FormatBuilder clientOf(String url) {
        return builder.get().clientOf(url, null);
    }

    public FormatBuilder clientOf(String url, Map<String, String> headers) {
        return builder.get().clientOf(url, headers);
    }
}
