package com.codeaffine.tiny.star.common;

import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static com.codeaffine.tiny.star.common.Reflections.Mode.FORWARD_RUNTIME_EXCEPTIONS;
import static com.codeaffine.tiny.star.common.Reflections.extractExceptionToReport;
import static com.codeaffine.tiny.star.common.Texts.ERROR_TIMEOUT_CALLING_RUNNABLE;
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
            cause.printStackTrace();
            currentThread().interrupt();
            exceptionHandler.accept(extractExceptionToReport(cause, IllegalStateException::new));
        } catch (TimeoutException cause) {
            String message = format(ERROR_TIMEOUT_CALLING_RUNNABLE, timeout, timeUnit, runnable.getClass().getName());
            exceptionHandler.accept(extractExceptionToReport(cause, throwable -> new IllegalStateException(message, throwable)));
        } catch (ExecutionException cause) {
            exceptionHandler.accept(extractExceptionToReport(cause, IllegalStateException::new, FORWARD_RUNTIME_EXCEPTIONS));
        }
    }

    public static void saveRun(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception cause) {
            cause.printStackTrace();
        }
    }
}
