package com.codeaffine.tiny.star;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Method;

public class SystemInSupplier implements InvocationInterceptor, ParameterResolver {

    private PipedOutputStream outputStream;

    public OutputStream getSupplierOutputStream() {
        return outputStream;
    }

    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext)
        throws Throwable
    {
        InputStream buffer = System.in;
        PipedInputStream inputStream = new PipedInputStream();
        outputStream = new PipedOutputStream(inputStream);
        System.setIn(inputStream);
        try {
            invocation.proceed();
        } finally {
            outputStream = null;
            System.setIn(buffer);
        }
    }

    @Override
    public boolean supportsParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return getClass() == parameterContext.getParameter().getType();
    }

    @Override
    public Object resolveParameter(
        ParameterContext parameterContext,
        ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return this;
    }
}
