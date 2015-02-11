package com.google.sitebricks.binding;

import com.google.sitebricks.headless.Request;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
public interface RequestBinder {
    String COLLECTION_BIND_PREFIX = "[C/";

    void bind(Request request, Object o);
}
