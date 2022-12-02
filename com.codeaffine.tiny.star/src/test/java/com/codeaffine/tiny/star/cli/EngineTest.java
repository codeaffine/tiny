package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.ThreadTestHelper.sleepFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;

class EngineTest {

    private static final int THREAD_SWITCH_TIME = 10;

    private ExecutorService executorService;
    private InputScanner scanner;
    private Engine engine;

    @BeforeEach
    void setUp() {
        executorService = newSingleThreadExecutor();
        scanner = mock(InputScanner.class);
        engine = new Engine(new ExecutorServiceAdapter(executorService), scanner);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executorService.shutdownNow();
        boolean isTerminated = executorService.awaitTermination(THREAD_SWITCH_TIME, MILLISECONDS);
        assertThat(isTerminated).isTrue();
    }
    @Test
    void start() {
        engine.start();
        sleepFor(THREAD_SWITCH_TIME);

        verify(scanner).scanForCommandCode();
    }

    @Test
    void stop() {
        engine.stop();
        sleepFor(THREAD_SWITCH_TIME);

        verify(scanner).cancel();
        assertThat(executorService.isTerminated()).isTrue();
    }

    @Test
    void constructWithNullAsExecutorArgument() {
        assertThatThrownBy(() -> new Engine(null, scanner))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsScannerArgument() {
        ExecutorServiceAdapter executor = new ExecutorServiceAdapter(executorService);

        assertThatThrownBy(() -> new Engine(executor, null))
            .isInstanceOf(NullPointerException.class);
    }
}
