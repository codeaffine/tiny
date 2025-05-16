/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static com.codeaffine.tiny.shared.Reflections.ExceptionExtractionMode.FORWARD_RUNTIME_EXCEPTIONS;
import static com.codeaffine.tiny.shared.Reflections.ExceptionExtractionMode.WRAP_RUNTIME_EXCEPTIONS;
import static com.codeaffine.tiny.shared.Texts.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;

/**
 * Utility class for performing common reflection-related operations.
 */
@NoArgsConstructor(access = PRIVATE)
public class Reflections {

    /**
     * Exception extraction handling modes.
     */
    public enum ExceptionExtractionMode {
        /**
         * Forward runtime exceptions as is.
         */
        FORWARD_RUNTIME_EXCEPTIONS,
        /**
         * Wrap runtime exceptions in a custom runtime exception.
         */
        WRAP_RUNTIME_EXCEPTIONS
    }

    /**
     * Creates a new instance of the specified class using its empty argument constructor.
     *
     * @param type The class type to create an instance of. Must not be null.
     * @param <T>  The type of the class to create an instance of.
     * @return A new instance of the specified class.
     */
    public static <T> T newInstance(@NonNull Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true); // NOSONAR
            return type.cast(constructor.newInstance());
        } catch (Exception cause) {
            throw extractExceptionToReport(cause, IllegalArgumentException::new, FORWARD_RUNTIME_EXCEPTIONS);
        }
    }

    /**
     * Extracts an exception to report, wrapping it in a custom runtime exception if necessary.
     *
     * @param exception The exception to extract. Must not be null.
     * @param runtimeExceptionFactory A factory function to create a custom runtime exception. Must not be null.
     * @param <T> The type of the custom runtime exception.
     * @return The extracted exception, wrapped in a custom runtime exception if necessary.
     */
    public static <T extends RuntimeException> RuntimeException extractExceptionToReport(
        @NonNull Exception exception,
        @NonNull Function<Throwable, T> runtimeExceptionFactory)
    {
        return extractExceptionToReport(exception, runtimeExceptionFactory, WRAP_RUNTIME_EXCEPTIONS);
    }

    /**
     * Extracts an exception to report, wrapping it in a custom runtime exception if necessary.
     *
     * @param exception The exception to extract. Must not be null.
     * @param runtimeExceptionFactory A factory function to create a custom runtime exception. Must not be null.
     * @param mode The mode for extracting the exception. Must not be null.
     * @param <T> The type of the custom runtime exception.
     * @return The extracted exception, wrapped in a custom runtime exception if necessary.
     */
    public static <T extends RuntimeException> RuntimeException extractExceptionToReport(
        @NonNull Exception exception,
        @NonNull Function<Throwable, T> runtimeExceptionFactory,
        @NonNull ExceptionExtractionMode mode)
    {
        RuntimeException result;
        T wrappedException = wrapException(exception, runtimeExceptionFactory);
        if(FORWARD_RUNTIME_EXCEPTIONS.equals(mode) && exception instanceof RuntimeException runtimeException) {
            result = runtimeException;
        } else if(wrappedException.getClass().isAssignableFrom(exception.getClass()) ) {
            //noinspection DataFlowIssue :: this is a valid cast since the exception is of the same type as the wrapped exception
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
