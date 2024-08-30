/**
 * <p>Copyright (c) 2022-2024 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.shared;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.codeaffine.tiny.test.test.fixtures.SystemPrintStreamCaptor.SystemErrCaptor;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ThreadsTest {

    public static final long TIMEOUT = 40;

    @Test
    void sleepFor() {
        long start = System.currentTimeMillis();
        Threads.sleepFor(TIMEOUT);
        long end = System.currentTimeMillis();

        assertThat(end - start).isGreaterThanOrEqualTo(TIMEOUT);
    }

    @Test
    void sleepForIfInterrupted() {
        AtomicReference<Thread> threadCaptor = new AtomicReference<>();

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            threadCaptor.set(currentThread());
            Threads.sleepFor(1000L);
        });
        Threads.sleepFor(TIMEOUT);
        threadCaptor.get().interrupt();
        Exception actual = catchException(future::get);

        assertThat(actual.getCause())
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(InterruptedException.class);
    }

    @Test
    void runAsyncAwaitingTermination() {
        AtomicBoolean executionCaptor = new AtomicBoolean();
        AtomicReference<Exception> exceptionCaptor = new AtomicReference<>();

        Threads.runAsyncAwaitingTermination(() -> executionCaptor.set(true), exceptionCaptor::set, TIMEOUT, MILLISECONDS);

        assertThat(executionCaptor.get()).isTrue();
        assertThat(exceptionCaptor.get()).isNull();
    }

    @Test
    void runAsyncAwaitingTerminationIfRunnableArgumentIsErrorProne() {
        AtomicReference<Exception> exceptionCaptor = new AtomicReference<>();
        RuntimeException expected = new RuntimeException("bad");

        Threads.runAsyncAwaitingTermination(() -> { throw expected; }, exceptionCaptor::set, TIMEOUT, MILLISECONDS);

        assertThat(exceptionCaptor.get()).isSameAs(expected);
    }

    @Test
    void runAsyncAwaitingTerminationIfExecutionTakesTooLong() {
        AtomicReference<Exception> exceptionCaptor = new AtomicReference<>();
        Runnable runnable = () -> Threads.sleepFor(TIMEOUT * 2);

        Threads.runAsyncAwaitingTermination(runnable, exceptionCaptor::set, TIMEOUT, MILLISECONDS);

        assertThat(exceptionCaptor.get())
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(TimeoutException.class)
            .hasMessageContaining(String.valueOf(TIMEOUT))
            .hasMessageContaining(MILLISECONDS.name())
            .hasMessageContaining(runnable.getClass().getName());
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    void runAsyncAwaitingTerminationGetsInterrupted(SystemErrCaptor systemErrCaptor) {
        AtomicReference<Exception> exceptionCaptor = new AtomicReference<>();
        AtomicReference<Thread> threadCaptor = new AtomicReference<>();
        Runnable runnable = () -> Threads.sleepFor(TIMEOUT * 2);

        runAsync(() -> {
            threadCaptor.set(currentThread());
            Threads.runAsyncAwaitingTermination(runnable, exceptionCaptor::set, TIMEOUT, MILLISECONDS);
        });
        Threads.sleepFor(TIMEOUT / 2);
        threadCaptor.get().interrupt();
        Threads.sleepFor(TIMEOUT / 2);

        assertThat(exceptionCaptor.get())
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(InterruptedException.class);
        assertThat(systemErrCaptor.getLog())
                .contains(InterruptedException.class.getName())
                .contains("Threads.runAsyncAwaitingTermination");
    }

    @Test
    void runAsyncAwaitingTerminationWithNullAsRunnableArgument() {
        assertThatThrownBy(() -> Threads.runAsyncAwaitingTermination(null, e -> {}, TIMEOUT, MILLISECONDS))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void runAsyncAwaitingTerminationWithNullAsExceptionConsumerArgument() {
        assertThatThrownBy(() -> Threads.runAsyncAwaitingTermination(() -> {}, null, TIMEOUT, MILLISECONDS))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void runAsyncAwaitingTerminationWithNullAsTimeUnitArgument() {
        assertThatThrownBy(() -> Threads.runAsyncAwaitingTermination(() -> {}, e -> {}, TIMEOUT, null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void saveRun() {
        Runnable runnable = mock(Runnable.class);

        Threads.saveRun(runnable);

        verify(runnable).run();
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    void saveRunWithRunnableArgumentThatThrowsRuntimeException(SystemErrCaptor systemErrCaptor) {
        Runnable runnable = mock(Runnable.class);
        RuntimeException expected = new RuntimeException("bad");
        doThrow(expected).when(runnable).run();

        Threads.saveRun(runnable);

        assertThat(systemErrCaptor.getLog()).contains(expected.getMessage());
    }

    private static void runAsync(Runnable runnable) {
        new Thread(runnable).start();
    }
}
