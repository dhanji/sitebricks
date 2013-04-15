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
    
    private final RequestBinder binder;
    
    private final Validator validator;

    @Inject
    public MutiPartFormTransport(Provider<HttpServletRequest> requestProvider, RequestBinder binder, Validator validator) {
        this.httpServletRequest = requestProvider.get();
        this.binder = binder;
        this.validator = validator;
    }

    public <T> T in(InputStream in, Class<T> type) throws IOException {
        T t = null;
        try {
            t = (T) type.newInstance();
            Request multiPartRequest = new MultiPartRequest(params(httpServletRequest));
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
            Request multiPartRequest = new MultiPartRequest(params(httpServletRequest));
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

    private Multimap<String, String> params(HttpServletRequest request) throws FileUploadException {
        
        ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
        FileItemFactory fileItemFactory = new DiskFileItemFactory(1000, null);

        ServletFileUpload upload = new ServletFileUpload(fileItemFactory);
        upload.setHeaderEncoding(request.getCharacterEncoding());
        List<FileItem> items = upload.parseRequest(request);
        
        Iterator<FileItem> iter = items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();
            if (item.isFormField()) {
                builder.put(item.getFieldName(), item.getString());
            } else {
                try {
                    // Use ISO-8859-1 encoding, see http://stackoverflow.com/questions/9098022/problems-converting-byte-array-to-string-and-back-to-byte-array
                    // When reading back the String to get the byte array, use getBytes("ISO-8859-1")
                    builder.put(item.getFieldName(), new String(item.get(), "ISO-8859-1"));
                }
                catch (UnsupportedEncodingException e) {
                }
            }
        }
        
        return builder.build();

    }

}
