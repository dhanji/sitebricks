package com.google.sitebricks.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.sitebricks.headless.Request;
import com.google.sitebricks.validation.SitebricksValidator;

public class MultiPartRequest implements Request {
    
    private final HttpServletRequest httpServletRequest;
    
    private SitebricksValidator validator;
    
    private Multimap<String, String> params;
    
    @Inject
    public MultiPartRequest(Provider<HttpServletRequest> requestProvider, SitebricksValidator validator) throws FileUploadException {
        this.httpServletRequest = requestProvider.get();
        this.validator = validator;
        this.params = params(this.httpServletRequest);
    }

    @Override
    public <E> RequestRead<E> read(Class<E> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <E> RequestRead<E> read(TypeLiteral<E> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readTo(OutputStream out) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Multimap<String, String> headers() {
        throw new UnsupportedOperationException();
    }   

    @Override
    public Multimap<String, String> params() {
        return this.params;
    }

    @Override
    public Multimap<String, String> matrix() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String matrixParam(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String param(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String header(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String uri() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String path() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String context() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String method() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validate(Object object) {
        Set<? extends ConstraintViolation<?>> cvs = validator.validate(object);
        if ((cvs != null) && (! cvs.isEmpty())) {
            throw new ValidationException(new ConstraintViolationException((Set<ConstraintViolation<?>>) cvs));
        }
    }
    
    private Multimap<String, String> params(HttpServletRequest request) throws FileUploadException {
//        ImmutableMultimap.Builder<String, FileItem> builder = ImmutableMultimap.builder();
//        FileItemFactory fileItemFactory = new DiskFileItemFactory(1000, null);
//
//        ServletFileUpload upload = new ServletFileUpload(fileItemFactory);
//        upload.setHeaderEncoding(request.getCharacterEncoding());
//        List<FileItem> items = upload.parseRequest(request);
//
//        Iterator<FileItem> iter = items.iterator();
//        while (iter.hasNext()) {
//            FileItem fileItem = (FileItem) iter.next();
//            builder.put(fileItem.getFieldName(), fileItem);
//        }
//
//        return builder.build();
      return null;
    }

}
