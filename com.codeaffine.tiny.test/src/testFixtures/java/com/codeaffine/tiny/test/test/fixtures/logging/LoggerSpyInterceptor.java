/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.test.test.fixtures.logging;

import lombok.SneakyThrows;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LoggerSpyInterceptor
    implements InvocationInterceptor
{

    record LoggerBuffer(Field field, Logger logger) {}

    @Override
    public void interceptTestMethod(
        Invocation<Void> invocation,
        ReflectiveInvocationContext<Method> invocationContext,
        ExtensionContext extensionContext)
        throws Throwable
    {
        Map<Class<?>, LoggerBuffer> loggerBuffers = new HashMap<>();
        extensionContext
            .getTestMethod()
            .ifPresent(method -> replaceLoggersWithSpy(
                loggerBuffers,
                () -> method.getAnnotation(UseLoggerSpy.class)
            ));
        if (loggerBuffers.isEmpty()) {
            extensionContext
                .getTestClass()
                .ifPresent(clazz -> replaceLoggersWithSpy(
                    loggerBuffers,
                    () -> clazz.getAnnotation(UseLoggerSpy.class)
                ));
        }
        try {
            invocation.proceed();
        } finally {
            loggerBuffers
                .values()
                .forEach(LoggerSpyInterceptor::restoreLogger);
        }
    }

    private static void replaceLoggersWithSpy(
        Map<Class<?>, LoggerBuffer> loggerBuffers,
        Supplier<UseLoggerSpy> annotationSupplier)
    {
        UseLoggerSpy annotation = annotationSupplier.get();
        if (nonNull(annotation)) {
            stream(annotation.value())
                .forEach(typeWithLogger -> replaceLoggerWithSpy(
                    loggerBuffers,
                    typeWithLogger
                ));
        }
    }

    private static void replaceLoggerWithSpy(
        Map<Class<?>, LoggerBuffer> loggerBuffers,
        Class<?> typeWithLogger)
    {
        Field loggerField = getLoggerField(typeWithLogger);
        Logger currentLogger = getCurrentLogger(loggerField);
        setNewLoggerSpy(loggerField);
        loggerBuffers.put(
            typeWithLogger,
            new LoggerBuffer(loggerField, currentLogger)
        );
    }

    private static Field getLoggerField(Class<?> typeWithLogger) {
        Field result = stream(typeWithLogger.getDeclaredFields())
            .filter(field -> field.getType().equals(Logger.class))
            .filter(field -> isStatic(field.getModifiers()))
            .findFirst()
            .orElseThrow(() -> noLoggerFound(typeWithLogger));
        result.setAccessible(true);
        return result;
    }

    private static IllegalArgumentException noLoggerFound(Class<?> type) {
        return new IllegalArgumentException(
            format("No static logger field found in '%s'.", type.getName())
        );
    }

    private static Logger getCurrentLogger(Field loggerField) {
        try {
            return (Logger) loggerField.get(null);
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException(iae);
        }
    }

    private static void setNewLoggerSpy(Field loggerField) {
        try {
            loggerField.set(null, stubLogger());
        } catch (IllegalAccessException iae) {
            throw new IllegalArgumentException(iae);
        }
    }

    private static Logger stubLogger() {
        Logger result = mock(Logger.class);
        when(result.isDebugEnabled()).thenReturn(true);
        when(result.isDebugEnabled(any())).thenReturn(true);
        when(result.isInfoEnabled()).thenReturn(true);
        when(result.isInfoEnabled(any())).thenReturn(true);
        when(result.isWarnEnabled()).thenReturn(true);
        when(result.isWarnEnabled(any())).thenReturn(true);
        when(result.isErrorEnabled()).thenReturn(true);
        when(result.isErrorEnabled(any())).thenReturn(true);
        return result;
    }

    @SneakyThrows
    private static void restoreLogger(LoggerBuffer loggerBuffer) {
        loggerBuffer.field.set(null, loggerBuffer.logger);
    }
}
