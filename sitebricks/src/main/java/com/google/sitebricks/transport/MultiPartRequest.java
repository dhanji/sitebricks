package com.google.sitebricks.transport;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.fileupload.FileItem;

import com.google.common.collect.Multimap;
import com.google.inject.TypeLiteral;
import com.google.sitebricks.headless.Request;

public class MultiPartRequest implements Request<FileItem> {
    
    private Multimap<String, FileItem> params;
    
    public MultiPartRequest(Multimap<String, FileItem> params) {
        this.params = params;
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
    public Multimap<String, FileItem> params() {
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
    public FileItem param(String name) {
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
    public void validate(Object obj) {
        throw new UnsupportedOperationException();
    }

}
