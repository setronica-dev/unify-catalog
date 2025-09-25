package com.setronica.unify.application.util;

import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;

public class PresizedInputStreamResource extends InputStreamResource {

    private long length;

    public PresizedInputStreamResource(InputStream inputStream, long length) {
        super(inputStream);
        this.length = length;
    }

    @Override
    public long contentLength() {
        return length;
    }
}
