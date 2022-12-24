package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.ThreadTestHelper.sleepFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.spi.CliCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

class EngineTest {

    private static final int THREAD_SWITCH_TIME = 10;
    private static final String CODE = "code";

    private Map<String, CliCommand> codeToCommandMap;
    private ExecutorService executorService;
    private InputScanner scanner;
    private Engine engine;

    @BeforeEach
    void setUp() {
        executorService = newSingleThreadExecutor();
        scanner = mock(InputScanner.class);
        codeToCommandMap = new HashMap<>();
        engine = new Engine(new ExecutorServiceAdapter(executorService), scanner, codeToCommandMap);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executorService.shutdownNow();
        boolean isTerminated = executorService.awaitTermination(THREAD_SWITCH_TIME, MILLISECONDS);
        assertThat(isTerminated).isTrue();
    }
    @Test
    void start() {
        int cliInstanceId = engine.addCliInstance(mock(ApplicationServer.class), Map.of(CODE, stubCliCommmand()));
        sleepFor(THREAD_SWITCH_TIME);

        verify(scanner).scanForCommandCode();
        assertThat(cliInstanceId).isZero();
    }

    @Test
    void stop() {
        Map<String, CliCommand> codeToCommandMap = Map.of(CODE, stubCliCommmand());
        int cliInstanceId = engine.addCliInstance(mock(ApplicationServer.class), codeToCommandMap);
        sleepFor(THREAD_SWITCH_TIME);

        engine.removeCliInstance(mock(ApplicationServer.class), cliInstanceId, codeToCommandMap);
        sleepFor(THREAD_SWITCH_TIME);

        verify(scanner).cancel();
        assertThat(executorService.isTerminated()).isTrue();
    }

    @Test
    void constructWithNullAsExecutorArgument() {
        assertThatThrownBy(() -> new Engine(null, scanner, codeToCommandMap))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructWithNullAsScannerArgument() {
        ExecutorServiceAdapter executor = new ExecutorServiceAdapter(executorService);

        assertThatThrownBy(() -> new Engine(executor, null, codeToCommandMap))
            .isInstanceOf(NullPointerException.class);
    }

    private static CliCommand stubCliCommmand() {
        CliCommand result = mock(CliCommand.class);
        when(result.getCode()).thenReturn(CODE);
        return result;
    }
}
