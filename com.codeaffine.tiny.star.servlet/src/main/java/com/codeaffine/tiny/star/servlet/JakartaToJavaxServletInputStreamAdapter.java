/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.servlet;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import javax.servlet.ReadListener;
import java.io.IOException;
import java.io.InputStream;

import static lombok.AccessLevel.PACKAGE;

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
