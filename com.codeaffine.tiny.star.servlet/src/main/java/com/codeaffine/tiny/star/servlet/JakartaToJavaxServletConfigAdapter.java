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
