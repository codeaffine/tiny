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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static com.codeaffine.tiny.shared.Reflections.ExceptionExtractionMode.FORWARD_RUNTIME_EXCEPTIONS;
import static com.codeaffine.tiny.shared.Reflections.extractExceptionToReport;
import static com.codeaffine.tiny.shared.Texts.ERROR_TIMEOUT_CALLING_RUNNABLE;
import static java.lang.String.format;
import static java.lang.Thread.*;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.concurrent.CompletableFuture.runAsync;
import static lombok.AccessLevel.PRIVATE;

/**
 * Utility class for thread-related operations.
 */
@NoArgsConstructor(access = PRIVATE)
public final class Threads {

    /**
     * <p>Sleeps for the specified number of milliseconds. This method is a
     * convenience method that wraps the {@link Thread#sleep(long)} method
     * and takes care of the checked {@link InterruptedException}.</p>
     * <p>One might use this method when a sleep interrupt is not expected. However,
     * if the sleep is interrupted unexpectedly, the according exception is caught,
     * the current thread is interrupted again and an {@link IllegalStateException}
     * with the original exception as cause gets thrown.</p>
     *
     * @param millis The number of milliseconds to sleep.
     * @throws IllegalStateException If the sleep is interrupted.
     */
    public static void sleepFor(long millis) throws IllegalStateException {
        try {
            sleep(millis); // NOSONAR
        } catch (InterruptedException interruptedException) {
            currentThread().interrupt();
            throw new IllegalStateException(interruptedException);
        }
    }

    /**
     * Runs a {@link Runnable} asynchronously and waits for the specified
     * timeout duration before terminating the thread.
     *
     * @param runnable The {@link Runnable} to run asynchronously. Must not be null.
     * @param exceptionHandler The {@link Consumer} to handle exceptions that occur
     *                         during the execution of the {@link Runnable}. Must
     *                         not be null.
     * @param timeout The timeout duration to wait for the {@link Runnable} to complete.
     *                Must be greater than 0.
     * @param timeUnit The {@link TimeUnit} of the timeout duration. Must not be null.
     *
     */
    public static void runAsyncAwaitingTermination(
        @NonNull Runnable runnable,
        @NonNull Consumer<Exception> exceptionHandler,
        long timeout,
        @NonNull TimeUnit timeUnit)
    {
        CompletableFuture<Void> future = runAsync(runnable);
        try {
            future.get(timeout, timeUnit);
        } catch (InterruptedException cause) {
            //noinspection CallToPrintStackTrace
            cause.printStackTrace(); // NOSONAR: on termination there might no longer be a logger available
            currentThread().interrupt();
            exceptionHandler.accept(extractExceptionToReport(cause, IllegalStateException::new));
        } catch (TimeoutException cause) {
            String message = format(ERROR_TIMEOUT_CALLING_RUNNABLE, timeout, timeUnit, runnable.getClass().getName());
            exceptionHandler.accept(extractExceptionToReport(cause, throwable -> new IllegalStateException(message, throwable)));
        } catch (ExecutionException cause) {
            exceptionHandler.accept(extractExceptionToReport(cause, IllegalStateException::new, FORWARD_RUNTIME_EXCEPTIONS));
        }
    }

    /**
     * Executes a {@link Runnable} and catches any exceptions that occur during its execution.
     * The exceptions are printed to the standard error stream without using a logger. This
     * method is intended for use in situations where a logger may not be available and
     * exceptions need to be handled gracefully. For example when an application is
     * shutting down and the logging framework has already been disposed.
     *
     * @param runnable The {@link Runnable} to execute. Must not be null.
     */
    public static void saveRunWithoutLogger(@NonNull Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception cause) {
            //noinspection CallToPrintStackTrace
            cause.printStackTrace(); // NOSONAR: on termination there might no longer be a logger available
        }
    }
}
