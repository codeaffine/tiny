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
