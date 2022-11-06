package com.codeaffine.tiny.star.servlet;

import static lombok.AccessLevel.PACKAGE;

import javax.servlet.ReadListener;

import java.io.IOException;
import java.io.InputStream;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor(access = PACKAGE)
class JakartaToJavaxServletInputStreamAdapter extends javax.servlet.ServletInputStream {

    @NonNull @Delegate
    private final InputStream delegate;

    @Override
    public int readLine(byte[] b, int off, int len) throws IOException {
        return ((jakarta.servlet.ServletInputStream)delegate).readLine(b, off, len);
    }

    @Override
    public boolean isFinished() {
        return ((jakarta.servlet.ServletInputStream)delegate).isFinished();
    }

    @Override
    public boolean isReady() {
        return ((jakarta.servlet.ServletInputStream)delegate).isReady();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        ((jakarta.servlet.ServletInputStream)delegate).setReadListener(new JavaxToJakartaReadListenerAdapter(readListener));
    }
}
