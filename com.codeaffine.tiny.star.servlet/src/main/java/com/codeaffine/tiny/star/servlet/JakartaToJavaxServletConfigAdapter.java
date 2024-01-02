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

import java.util.Enumeration;

import static lombok.AccessLevel.PACKAGE;

@SuppressWarnings("ClassCanBeRecord")
@RequiredArgsConstructor(access = PACKAGE)
class JakartaToJavaxServletConfigAdapter implements javax.servlet.ServletConfig {

    @NonNull
    final jakarta.servlet.ServletConfig delegate;

    @Override
    public String getServletName() {
        return delegate.getServletName();
    }

    @Override
    public javax.servlet.ServletContext getServletContext() {
        return new JakartaToJavaxServletContextAdapter(delegate.getServletContext());
    }

    @Override
    public String getInitParameter(String name) {
        return delegate.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return delegate.getInitParameterNames();
    }
}
