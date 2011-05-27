package com.google.sitebricks.binding;

import com.google.inject.ImplementedBy;
import com.google.sitebricks.headless.Request;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(MvelRequestBinder.class)
public interface RequestBinder {
    String COLLECTION_BIND_PREFIX = "[C/";

    void bind(Request request, Object o);
}
