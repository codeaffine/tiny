package com.codeaffine.tiny.star.servlet;

import static lombok.AccessLevel.PACKAGE;

import jakarta.servlet.WriteListener;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
class JavaxToJakartaWriteListenerAdapter implements WriteListener {

    @NonNull
    private final javax.servlet.WriteListener delegate;

    @Override
    public void onWritePossible() throws IOException {
        delegate.onWritePossible();
    }

    @Override
    public void onError(Throwable t) {
        delegate.onError(t);
    }
}
