package com.codeaffine.tiny.star.servlet;

import static lombok.AccessLevel.PACKAGE;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor(access = PACKAGE)
class JakartaToJavaxServletOutputStreamAdapter extends ServletOutputStream {

    @NonNull @Delegate
    private final jakarta.servlet.ServletOutputStream delegate;

    @Override
    public void setWriteListener(WriteListener writeListener) {
        delegate.setWriteListener(new JavaxToJakartaWriteListenerAdapter(writeListener));
    }
}
