package com.google.sitebricks.client;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import net.jcip.annotations.NotThreadSafe;

import java.util.Map;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@NotThreadSafe
class WebClientBuilder implements Web.FormatBuilder {

    private final Injector injector;

    private String url;
    private Map<String, String> headers;

    @Inject
    public WebClientBuilder(Injector injector) {
        this.injector = injector;
    }

    public Web.FormatBuilder clientOf(String url) {
        this.url = url;
        this.headers = null;

        return this;
    }

    public Web.FormatBuilder clientOf(String url, Map<String, String> headers) {
        this.url = url;
        this.headers = headers;

        return this;
    }

    public <T> Web.ReadAsBuilder<T> transports(Class<T> clazz) {
        return new InternalReadAsBuilder<T>(clazz);
    }

    private class InternalReadAsBuilder<T> implements Web.ReadAsBuilder<T>{
        private final Class<T> transporting;

        private InternalReadAsBuilder(Class<T> transporting) {
            this.transporting = transporting;
        }

        public WebClient<T> over(Class<? extends Transport> transport) {
            return new CommonsWebClient<T>(injector, url, headers, transporting, Key.get(transport));
        }
    }
}
