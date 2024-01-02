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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;
import java.util.Enumeration;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class JakartaToJavaxHttpSessionAdapter implements javax.servlet.http.HttpSession {

    @NonNull
    private final jakarta.servlet.http.HttpSession delegate;

    @Override
    public long getCreationTime() {
        return delegate.getCreationTime();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public long getLastAccessedTime() {
        return delegate.getLastAccessedTime();
    }

    @Override
    public ServletContext getServletContext() {
        return new JakartaToJavaxServletContextAdapter(delegate.getServletContext());
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        delegate.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        return delegate.getMaxInactiveInterval();
    }

    @Override
    @SuppressWarnings("deprecation")
    public HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String name) {
        return delegate.getAttribute(name);
    }

    @Override
    public Object getValue(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return delegate.getAttributeNames();
    }

    @Override
    public String[] getValueNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(String name, Object value) {
        delegate.setAttribute(name, value);
    }

    @Override
    public void putValue(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(String name) {
        delegate.removeAttribute(name);
    }

    @Override
    public void removeValue(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidate() {
        delegate.invalidate();
    }

    @Override
    public boolean isNew() {
        return delegate.isNew();
    }
}
