package com.google.sitebricks.client;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface WebClient<T> {
    WebResponse get();

    WebResponse post(T t);

    WebResponse put(T t);

    WebResponse patch(T t);

    WebResponse delete();

    /**
     * Close the underlying client.
     */
    void close();
}
