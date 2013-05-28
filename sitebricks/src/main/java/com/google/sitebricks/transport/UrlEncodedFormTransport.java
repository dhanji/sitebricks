package com.google.sitebricks.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import com.google.inject.TypeLiteral;
import com.google.sitebricks.binding.RequestBinder;
import com.google.sitebricks.headless.Request;

/**
 * A HTML Form (UTF-8) implementation of Transport where input types can 
 * be any object. This object will be binded to the {@link Request} params.
 */
class UrlEncodedFormTransport extends Form {
    
    private final Request<String> request;
    
    private final RequestBinder<String> binder;
    
    @Inject
    public UrlEncodedFormTransport(Request<String> request, RequestBinder<String> binder) {
        this.request = request;
        this.binder = binder;
    }

    public <T> T in(InputStream in, Class<T> type) throws IOException {
        T t = null;
        try {
           t = (T) type.newInstance();
           binder.bind(request, t);
           request.validate(t);
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
           binder.bind(request, t);
           request.validate(t);
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
