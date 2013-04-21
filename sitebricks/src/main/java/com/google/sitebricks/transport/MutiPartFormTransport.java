package com.google.sitebricks.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.sitebricks.binding.RequestBinder;
import com.google.sitebricks.headless.Request;

/**
 * A Multipart HTML Form (UTF-8) implementation of Transport where input types can be any
 * object. This object will be binded to the {@link Request} params.
 */
class MutiPartFormTransport extends MultiPartForm {

    private final HttpServletRequest httpServletRequest;
    
    private final RequestBinder<FileItem> binder;
    
    private final Validator validator;

    @Inject
    public MutiPartFormTransport(Provider<HttpServletRequest> requestProvider, RequestBinder<FileItem> binder, Validator validator) {
        this.httpServletRequest = requestProvider.get();
        this.binder = binder;
        this.validator = validator;
    }

    public <T> T in(InputStream in, Class<T> type) throws IOException {
        T t = null;
        try {
            t = (T) type.newInstance();
            Request<FileItem> multiPartRequest = new MultiPartRequest(params(httpServletRequest));
            binder.bind(multiPartRequest, t);
            // TODO(eric) should use request.validate(t) method...
            Set<? extends ConstraintViolation<?>> cvs = validator.validate(t);
            if ((cvs != null) && (! cvs.isEmpty())) {
                throw new ValidationException(new ConstraintViolationException((Set<ConstraintViolation<?>>) cvs));
            }
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        catch (FileUploadException e) {
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
            Request<FileItem> multiPartRequest = new MultiPartRequest(params(httpServletRequest));
            binder.bind(multiPartRequest, t);
            // TODO(eric) should use request.validate(t) method...
            Set<? extends ConstraintViolation<?>> cvs = validator.validate(t);
            if ((cvs != null) && (! cvs.isEmpty())) {
                throw new ValidationException(new ConstraintViolationException((Set<ConstraintViolation<?>>) cvs));
            }
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        catch (FileUploadException e) {
            throw new RuntimeException(e);
        }
        return t;
    }

    public <T> void out(OutputStream out, Class<T> type, T data) {
        throw new IllegalAccessError("You should not write to a form transport.");
    }

    private Multimap<String, FileItem> params(HttpServletRequest request) throws FileUploadException {
        
        ImmutableMultimap.Builder<String, FileItem> builder = ImmutableMultimap.builder();
        FileItemFactory fileItemFactory = new DiskFileItemFactory(1000, null);

        ServletFileUpload upload = new ServletFileUpload(fileItemFactory);
        upload.setHeaderEncoding(request.getCharacterEncoding());
        List<FileItem> items = upload.parseRequest(request);
        
        Iterator<FileItem> iter = items.iterator();
        while (iter.hasNext()) {
            FileItem fileItem = (FileItem) iter.next();
            builder.put(fileItem.getFieldName(), fileItem);
        }
        
        return builder.build();

    }

}
