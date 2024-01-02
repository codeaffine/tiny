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

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import static lombok.AccessLevel.PACKAGE;

@RequiredArgsConstructor(access = PACKAGE)
class JakartaToJavaxServletResponseAdapter implements javax.servlet.http.HttpServletResponse {

    @NonNull
    private final jakarta.servlet.http.HttpServletResponse delegate;

    @Override
    public void addCookie(javax.servlet.http.Cookie cookie) {
        delegate.addCookie(new jakarta.servlet.http.Cookie(cookie.getName(), cookie.getValue()));
    }

    @Override
    public boolean containsHeader(String name) {
        return delegate.containsHeader(name);
    }

    @Override
    public String encodeURL(String url) {
        return delegate.encodeURL(url);
    }

    @Override
    public String encodeRedirectURL(String url) {
        return delegate.encodeRedirectURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectUrl(String url) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        delegate.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        delegate.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        delegate.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        delegate.setDateHeader(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        delegate.addDateHeader(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        delegate.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        delegate.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        delegate.setIntHeader(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        delegate.addIntHeader(name, value);
    }

    @Override
    public void setStatus(int sc) {
        delegate.setStatus(sc);
    }

    @Override
    public void setStatus(int sc, String sm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() {
        return delegate.getStatus();
    }

    @Override
    public String getHeader(String name) {
        return delegate.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return delegate.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return delegate.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return delegate.getCharacterEncoding();
    }

    @Override
    public String getContentType() {
        return delegate.getContentType();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new JakartaToJavaxServletOutputStreamAdapter(delegate.getOutputStream());
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return delegate.getWriter();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        delegate.setCharacterEncoding(charset);
    }

    @Override
    public void setContentLength(int len) {
        delegate.setContentLength(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        delegate.setContentLength((int) len);
    }

    @Override
    public void setContentType(String type) {
        delegate.setContentType(type);
    }

    @Override
    public void setBufferSize(int size) {
        delegate.setBufferSize(size);
    }

    @Override
    public int getBufferSize() {
        return delegate.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        delegate.flushBuffer();
    }

    @Override
    public void resetBuffer() {
        delegate.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return delegate.isCommitted();
    }

    @Override
    public void reset() {
        delegate.reset();
    }

    @Override
    public void setLocale(Locale loc) {
        // unused
    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
