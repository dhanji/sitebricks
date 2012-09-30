package com.google.sitebricks.client;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Executor;

/**
 * Synchronous and async http client.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface WebClient<T> {
    WebResponse get();

    WebResponse post(T t);

    WebResponse put(T t);

    WebResponse patch(T t);

    WebResponse delete();

    ListenableFuture<WebResponse> get(Executor executor);

    ListenableFuture<WebResponse> post(T t, Executor executor);

    ListenableFuture<WebResponse> put(T t, Executor executor);

    ListenableFuture<WebResponse> patch(T t, Executor executor);

    ListenableFuture<WebResponse> delete(Executor executor);

    /**
     * Close the underlying client.
     */
    void close();
}
