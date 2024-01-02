/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.servlet;

import jakarta.servlet.WriteListener;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

import static lombok.AccessLevel.PACKAGE;

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
