package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.SystemPrintStreamCaptor.*;
import static com.codeaffine.tiny.star.ThreadTestHelper.sleepFor;
import static com.codeaffine.tiny.star.cli.Messages.ERROR_AWAITING_SHUT_DOWN_CLI;
import static com.codeaffine.tiny.star.cli.Messages.ERROR_SHUTING_DOWN_CLI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class ExecutorServiceAdapterTest {

    private static final int TEST_TIMEOUT_AWAITING_TERMINATION = 20;
    private static final int MILLIS_IN_WHICH_SERVER_IS_EXPECTED_TO_BE_HALTED = TEST_TIMEOUT_AWAITING_TERMINATION * 2;

    private ExecutorServiceAdapter executor;
    private ExecutorService executorService;

    private final Object lock = new Object();
    private volatile boolean cancelRunningTask;

    @BeforeEach
    void setUp() {
        cancelRunningTask = false;
        executorService = newSingleThreadExecutor();
        executor = new ExecutorServiceAdapter(executorService, TEST_TIMEOUT_AWAITING_TERMINATION);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        cancelRunningTask = true;
        executorService.shutdownNow();
        boolean isTerminated = executorService.awaitTermination(100, MILLISECONDS);
        assertThat(isTerminated).isTrue();
    }

    @Test
    void execute() {
        Runnable runnable = mock(Runnable.class);

        executor.execute(runnable);
        sleepFor(50);

        verify(runnable).run();
    }

    @Test
    void stop() {
        AtomicReference<Object> threadCaptor = new AtomicReference<>();
        AtomicBoolean canceledCaptor = new AtomicBoolean(false);

        executor.execute(() -> blockButRespectCancelRequestByInterrupt(threadCaptor, canceledCaptor));
        executor.stop();
        sleepFor(MILLIS_IN_WHICH_SERVER_IS_EXPECTED_TO_BE_HALTED);

        assertThat(threadCaptor.get()).isNotSameAs(currentThread());
        assertThat(canceledCaptor.get()).isTrue();
        assertThat(executorService.isShutdown()).isTrue();
    }

    @Test
    void stopMoreThanOnce() {
        AtomicReference<Object> threadCaptor = new AtomicReference<>();
        AtomicBoolean canceledCaptor = new AtomicBoolean(false);

        executor.execute(() -> blockButRespectCancelRequestByInterrupt(threadCaptor, canceledCaptor));
        executor.stop();
        sleepFor(MILLIS_IN_WHICH_SERVER_IS_EXPECTED_TO_BE_HALTED);
        executor.stop();
        sleepFor(MILLIS_IN_WHICH_SERVER_IS_EXPECTED_TO_BE_HALTED);

        assertThat(threadCaptor.get()).isNotSameAs(currentThread());
        assertThat(canceledCaptor.get()).isTrue();
        assertThat(executorService.isShutdown()).isTrue();
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    void stopWithBlockingTask(SystemErrCaptor systemErrCaptor) {
        AtomicReference<Object> threadCaptor = new AtomicReference<>();
        AtomicBoolean canceledCaptor = new AtomicBoolean(false);

        executor.execute(() -> blockAndIgnoreShutdownRequestByInterrupt(threadCaptor, canceledCaptor));
        executor.stop();
        sleepFor(MILLIS_IN_WHICH_SERVER_IS_EXPECTED_TO_BE_HALTED);

        assertThat(systemErrCaptor.getLog()).contains(ERROR_AWAITING_SHUT_DOWN_CLI);
    }

    @Test
    @ExtendWith(SystemErrCaptor.class)
    void stopIfShutdownHandlerThreadWouldBeSomhowInterrupted(SystemErrCaptor systemErrCaptor) {
        executor = new ExecutorServiceAdapter(executorService, MILLIS_IN_WHICH_SERVER_IS_EXPECTED_TO_BE_HALTED);

        executor.execute(() -> blockAndIgnoreShutdownRequestByInterrupt(new AtomicReference<>(), new AtomicBoolean()));
        executor.stop();
        executor.shutdownHandler.interrupt();
        sleepFor(MILLIS_IN_WHICH_SERVER_IS_EXPECTED_TO_BE_HALTED);

        assertThat(systemErrCaptor.getLog()).contains(ERROR_SHUTING_DOWN_CLI);
    }

    @Test
    void constructWithNullAsArgumentNameArgument() {
        assertThatThrownBy(() -> new ExecutorServiceAdapter(null))
            .isInstanceOf(NullPointerException.class);
    }

    private void blockButRespectCancelRequestByInterrupt(AtomicReference<Object> threadCaptor, AtomicBoolean canceledCaptor) {
        threadCaptor.set(currentThread());
        try {
            synchronized (lock) {
                lock.wait();
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
            canceledCaptor.set(true);
        }
    }

    private void blockAndIgnoreShutdownRequestByInterrupt(AtomicReference<Object> threadCaptor, AtomicBoolean canceledCaptor) {
        threadCaptor.set(currentThread());
        while (!cancelRunningTask) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    // simulate bad behaving task and ignore interrupt
                    canceledCaptor.set(true);
                }
            }
        }
    }
}
