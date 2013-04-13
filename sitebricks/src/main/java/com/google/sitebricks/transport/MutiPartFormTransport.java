package com.google.sitebricks.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.ParameterParser;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.TypeLiteral;
import com.google.sitebricks.binding.RequestBinder;
import com.google.sitebricks.headless.Request;

/**
 * A Multipart HTML Form (UTF-8) implementation of Transport where input types can be any
 * object. This object will be binded to the {@link Request} params.
 */
class MutiPartFormTransport extends MultiPartForm {

    private static final String CONTENT_DISPOSITION = "content-disposition";

    private final Request request;
    
    private final RequestBinder binder;
    
    private final Validator validator;

    @Inject
    public MutiPartFormTransport(Request request, RequestBinder binder, Validator validator) {
        this.request = request;
        this.binder = binder;
        this.validator = validator;
    }

    public <T> T in(InputStream in, Class<T> type) throws IOException {
        T t = null;
        try {
            t = (T) type.newInstance();
            Request multiPartRequest = new MultiPartRequest(params(in));
            binder.bind(multiPartRequest, t);
            // TODO should use request.validate(t) method...
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
        return t;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T in(InputStream in, TypeLiteral<T> type) throws IOException {
        T t = null;
        try {
            t = (T) type.getRawType().newInstance();
            Request multiPartRequest = new MultiPartRequest(params(in));
            binder.bind(multiPartRequest, t);
            // TODO should use request.validate(t) method...
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
        return t;
    }

    public <T> void out(OutputStream out, Class<T> type, T data) {
        throw new IllegalAccessError("You should not write to a form transport.");
    }

    private Multimap<String, String> params(InputStream in) throws IOException {
        
        ImmutableMultimap.Builder<String, String> paramsBuilder = ImmutableMultimap.builder();

        byte[] boundary = getBoundary(request.header("Content-Type"));

        @SuppressWarnings("deprecation")
        MultipartStream multipartStream = new MultipartStream(in, boundary);

        boolean nextPart = multipartStream.skipPreamble();
        while (nextPart) {
            String header = multipartStream.readHeaders();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            multipartStream.readBodyData(baos);
            byte[] value = baos.toByteArray();
            paramsBuilder.put(getFieldName(header), new String(value));
            nextPart = multipartStream.readBoundary();
        }

        return paramsBuilder.build();

    }

    private byte[] getBoundary(String contentType) {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        char[] separators = new char[] { ';', ',' };
        Map<String, String> params = parser.parse(contentType, separators);
        String boundaryStr = (String) params.get("boundary");

        if (boundaryStr == null) {
            return null;
        }
        byte[] boundary;
        try {
            boundary = boundaryStr.getBytes("ISO-8859-1");
        }
        catch (UnsupportedEncodingException e) {
            boundary = boundaryStr.getBytes();
        }
        return boundary;
    }

    /**
     * Returns the field name, which is given by the content-disposition header.
     * 
     * @param pContentDisposition
     *            The content-dispositions header value.
     * @return The field jake
     */
    private String getFieldName(String pContentDisposition) {
        String fieldName = null;
        if (pContentDisposition != null && pContentDisposition.toLowerCase().startsWith(CONTENT_DISPOSITION)) {
            ParameterParser parser = new ParameterParser();
            parser.setLowerCaseNames(true);
            // Parameter parser can handle null input
            Map params = parser.parse(pContentDisposition, ';');
            fieldName = (String) params.get("name");
            if (fieldName != null) {
                fieldName = fieldName.trim();
            }
        }
        return fieldName;
    }

}
