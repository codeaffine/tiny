package com.codeaffine.tiny.star.servlet;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.servlet.ReadListener;
import java.io.IOException;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class JavaxToJakartaReadListenerAdapter implements jakarta.servlet.ReadListener {

    @NonNull
    private final ReadListener delegate;

    @Override
    public void onDataAvailable() throws IOException {
        delegate.onDataAvailable();
    }

    @Override
    public void onAllDataRead() throws IOException {
        delegate.onAllDataRead();
    }

    @Override
    public void onError(Throwable t) {
        delegate.onError(t);
    }
}
