package com.codeaffine.tiny.star.common;

import static com.codeaffine.tiny.star.ThreadTestHelper.sleepFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.junit.jupiter.api.Test;

import com.codeaffine.tiny.star.ThreadTestHelper;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class ThreadsTest {

    public static final long TIMEOUT = 40;

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
        Runnable runnable = () -> sleepFor(TIMEOUT * 2);

        Threads.runAsyncAwaitingTermination(runnable, exceptionCaptor::set, TIMEOUT, MILLISECONDS);

        assertThat(exceptionCaptor.get())
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(TimeoutException.class)
            .hasMessageContaining(String.valueOf(TIMEOUT))
            .hasMessageContaining(MILLISECONDS.name())
            .hasMessageContaining(runnable.getClass().getName());
    }

    @Test
    void runAsyncAwaitingTerminationGetsInterrupted() {
        AtomicReference<Exception> exceptionCaptor = new AtomicReference<>();
        AtomicReference<Thread> threadCaptor = new AtomicReference<>();
        Runnable runnable = () -> sleepFor(TIMEOUT * 2);

        runAsync(() -> {
            threadCaptor.set(currentThread());
            Threads.runAsyncAwaitingTermination(runnable, exceptionCaptor::set, TIMEOUT, MILLISECONDS);
        });
        sleepFor(TIMEOUT / 2);
        threadCaptor.get().interrupt();
        sleepFor(TIMEOUT / 2);

        assertThat(exceptionCaptor.get())
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(InterruptedException.class);
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

    private static void runAsync(Runnable runnable) {
        new Thread(runnable).start();
    }
}
