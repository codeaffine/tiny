/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.test.test.fixtures.system.io;

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
