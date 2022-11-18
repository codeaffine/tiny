package com.codeaffine.tiny.star;

import static java.nio.charset.StandardCharsets.UTF_8;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SystemPrintStreamCaptor implements InvocationInterceptor, ParameterResolver {

    private final Consumer<PrintStream> replacementSetter;
    private final Supplier<PrintStream> originalCaptor;
    private final ByteArrayOutputStream outputStream;

    public static class SystemOutCaptor extends SystemPrintStreamCaptor {
        public SystemOutCaptor() {
            super(() -> System.out, System::setOut);
        }
    }

    public static class SystemErrCaptor extends SystemPrintStreamCaptor {
        public SystemErrCaptor() {
            super(() -> System.err, System::setErr);
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
