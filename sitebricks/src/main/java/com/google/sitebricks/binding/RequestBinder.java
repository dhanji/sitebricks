package com.google.sitebricks.binding;

import com.google.inject.ImplementedBy;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail.com)
 */
@ImplementedBy(MvelRequestBinder.class)
public interface RequestBinder {
    String COLLECTION_BIND_PREFIX = "[C/";

    void bind(HttpServletRequest request, Object o);
}
