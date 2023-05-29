/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static com.codeaffine.tiny.shared.Reflections.Mode.*;
import static com.codeaffine.tiny.shared.Texts.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Reflections {

    public enum Mode {
        FORWARD_RUNTIME_EXCEPTIONS,
        WRAP_NON_ASSIGNABLE_RUNTIME_EXCEPTIONS
    }

    public static <T extends RuntimeException> RuntimeException extractExceptionToReport(
        @NonNull Exception exception,
        @NonNull Function<Throwable, T> runtimeExceptionFactory)
    {
        return extractExceptionToReport(exception, runtimeExceptionFactory, WRAP_NON_ASSIGNABLE_RUNTIME_EXCEPTIONS);
    }

    public static <T extends RuntimeException> RuntimeException extractExceptionToReport(
        @NonNull Exception exception,
        @NonNull Function<Throwable, T> runtimeExceptionFactory,
        @NonNull Mode mode)
    {
        RuntimeException result;
        T wrappedException = wrapException(exception, runtimeExceptionFactory);
        if(FORWARD_RUNTIME_EXCEPTIONS.equals(mode) && exception instanceof RuntimeException runtimeException) {
            result = runtimeException;
        } else if(wrappedException.getClass().isAssignableFrom(exception.getClass()) ) {
            result = (RuntimeException)exception;
        } else if(exception instanceof ExecutionException executionException && executionException.getCause() instanceof Exception cause) {
            result = extractExceptionToReport(cause, runtimeExceptionFactory, mode);
        } else if (exception instanceof InvocationTargetException itc && itc.getCause() instanceof Exception cause) {
            result = extractExceptionToReport(cause, runtimeExceptionFactory, mode);
        } else {
            result = wrappedException;
        }
        return result;
    }

    private static <T extends RuntimeException> T wrapException(Exception exception, Function<Throwable, T> runtimeExceptionFactory) {
        T result = runtimeExceptionFactory.apply(exception);
        if (isNull(result)) {
            throw new IllegalArgumentException(ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_NULL);
        }
        if(isNull(result.getCause())) {
            throw new IllegalArgumentException(ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_WRAPPER_WITHOUT_CAUSE);
        }
        if(nonNull(result.getCause()) && !result.getCause().equals(exception)) {
            throw new IllegalArgumentException(ERROR_ARGUMENT_RUNTIME_EXCEPTION_FACTORY_RETURN_WRAPPER_WITH_WRONG_CAUSE);
        }
        return result;
    }
}
