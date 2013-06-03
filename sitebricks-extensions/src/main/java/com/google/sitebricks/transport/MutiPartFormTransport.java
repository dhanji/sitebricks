package com.google.sitebricks.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import org.apache.commons.fileupload.FileItem;

import com.google.inject.TypeLiteral;
import com.google.sitebricks.binding.RequestBinder;
import com.google.sitebricks.headless.Request;

/**
 * A Multipart HTML Form (UTF-8) implementation of Transport where input types can be any
 * object. This object will be binded to the {@link Request} params.
 */
class MutiPartFormTransport extends MultiPartForm {
    
    private final MultiPartRequest multiPartRequest;

    private final RequestBinder<FileItem> binder;
    
    @Inject
    public MutiPartFormTransport(MultiPartRequest multiPartRequest, RequestBinder<FileItem> binder) {
        this.multiPartRequest = multiPartRequest;
        this.binder = binder;
    }

    public <T> T in(InputStream in, Class<T> type) throws IOException {
        T t = null;
        try {
            t = (T) type.newInstance();
            binder.bind(multiPartRequest, t);
            multiPartRequest.validate(t);
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T in(InputStream in, TypeLiteral<T> type) throws IOException {
        T t = null;
        try {
            t = (T) type.getRawType().newInstance();
            binder.bind(multiPartRequest, t);
            multiPartRequest.validate(t);
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    public <T> void out(OutputStream out, Class<T> type, T data) {
        throw new IllegalAccessError("You should not write to a form transport.");
    }

}
