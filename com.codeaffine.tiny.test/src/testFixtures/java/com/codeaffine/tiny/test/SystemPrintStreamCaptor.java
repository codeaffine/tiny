/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.test;

import org.junit.jupiter.api.extension.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SystemPrintStreamCaptor implements InvocationInterceptor, ParameterResolver {

    private final Consumer<PrintStream> replacementSetter;
    private final Supplier<PrintStream> originalCaptor;
    private final ByteArrayOutputStream outputStream;

    public static class SystemOutCaptor extends SystemPrintStreamCaptor {
        public SystemOutCaptor() {
            super(() -> System.out, System::setOut); // NOSONAR
        }
    }

    public static class SystemErrCaptor extends SystemPrintStreamCaptor {
        public SystemErrCaptor() {
            super(() -> System.err, System::setErr); // NOSONAR
        }
    }

    SystemPrintStreamCaptor(Supplier<PrintStream> originalCaptor, Consumer<PrintStream> replacementSetter) {
        this.outputStream = new ByteArrayOutputStream();
        this.replacementSetter = replacementSetter;
        this.originalCaptor = originalCaptor;
    }

    public String getLog() {
        return outputStream.toString(UTF_8);
    }

    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext)
        throws Throwable
    {
        PrintStream buffer = originalCaptor.get();
        replacementSetter.accept(new PrintStream(outputStream));
        try {
            invocation.proceed();
        } finally {
            replacementSetter.accept(buffer);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return getClass() == parameterContext.getParameter().getType();
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return this;
    }
}
