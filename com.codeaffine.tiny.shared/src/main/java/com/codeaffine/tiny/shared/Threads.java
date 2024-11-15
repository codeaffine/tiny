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

import static com.codeaffine.tiny.shared.Reflections.Mode.FORWARD_RUNTIME_EXCEPTIONS;
import static com.codeaffine.tiny.shared.Reflections.extractExceptionToReport;
import static com.codeaffine.tiny.shared.Texts.ERROR_TIMEOUT_CALLING_RUNNABLE;
import static java.lang.String.format;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;
import static java.util.concurrent.CompletableFuture.runAsync;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class Threads {

    public static void sleepFor(long millis) {
        try {
            sleep(millis); // NOSONAR
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(interruptedException);
        }
    }

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

    public static void saveRunWithoutLogger(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception cause) {
            //noinspection CallToPrintStackTrace
            cause.printStackTrace(); // NOSONAR: on termination there might no longer be a logger available
        }
    }
}
