package com.codeaffine.tiny.test;

import org.junit.jupiter.api.extension.*;

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
        try(PipedInputStream inputStream = new PipedInputStream()) {
            outputStream = new PipedOutputStream(inputStream);
            System.setIn(inputStream);
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
