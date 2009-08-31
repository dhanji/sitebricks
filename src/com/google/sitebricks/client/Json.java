package com.google.sitebricks.client;

import org.mvel.MVEL;
import org.mvbus.MVBus;
import org.mvbus.Configuration;
import org.mvbus.encode.engines.json.JsonDecodingEngine;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public class Json implements Transport {
    private final MVBus bus = MVBus.createBus(new Configuration() {
        protected void configure() {
            decodeUsing(new JsonDecodingEngine());
        }
    });

    public <T> T in(InputStream in, Class<T> type) throws IOException {
        return bus.decode(type, in);
    }

    public <T> void out(OutputStream out, Class<T> type, T data) {
        throw new UnsupportedOperationException();
    }
}
