/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.shared.Synchronizer;
import com.codeaffine.tiny.star.cli.spi.CliCommand;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static com.codeaffine.tiny.shared.SynchronizerTestHelper.fakeSynchronizer;
import static com.codeaffine.tiny.shared.Threads.sleepFor;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EngineTest {

    private static final int THREAD_SWITCH_TIME = 10;
    private static final String CODE = "code";

    private Map<String, CliCommand> codeToCommandMap;
    private ExecutorService executorService;
    private Synchronizer synchronizer;
    private InputScanner scanner;
    private Engine engine;

    @BeforeEach
    void setUp() {
        executorService = newSingleThreadExecutor();
        scanner = mock(InputScanner.class);
        codeToCommandMap = new HashMap<>();
        synchronizer = fakeSynchronizer();
        engine = new Engine(new ExecutorServiceAdapter(executorService), scanner, codeToCommandMap, synchronizer);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        executorService.shutdownNow();
        boolean isTerminated = executorService.awaitTermination(THREAD_SWITCH_TIME, MILLISECONDS);
        assertThat(isTerminated).isTrue();
    }

    @Test
    void start() {
        int cliInstanceId = engine.addCliInstance(mock(ApplicationServer.class), Map.of(CODE, stubCliCommand()));
        sleepFor(THREAD_SWITCH_TIME);
        boolean running = engine.isRunning();

        verify(scanner).scanForCommandCode();
        assertThat(cliInstanceId).isZero();
        assertThat(running).isTrue();
    }

    @Test
    void startWithoutSynchronization() {
        reset(synchronizer);

        Integer cliInstanceId = engine.addCliInstance(mock(ApplicationServer.class), Map.of(CODE, stubCliCommand()));
        sleepFor(THREAD_SWITCH_TIME);
        Boolean running = engine.isRunning();

        verify(scanner, never()).scanForCommandCode();
        assertThat(cliInstanceId).isNull();
        assertThat(running).isFalse();
    }

    @Test
    void stop() {
        Map<String, CliCommand> localCodeToCommandMap = Map.of(CODE, stubCliCommand());
        int cliInstanceId = engine.addCliInstance(mock(ApplicationServer.class), localCodeToCommandMap);
        sleepFor(THREAD_SWITCH_TIME);

        engine.removeCliInstance(mock(ApplicationServer.class), cliInstanceId, localCodeToCommandMap);
        sleepFor(THREAD_SWITCH_TIME);
        boolean running = engine.isRunning();

        verify(scanner).cancel();
        assertThat(executorService.isTerminated()).isTrue();
        assertThat(running).isFalse();
    }

    @Test
    void stopWithoutSynchronization() {
        Map<String, CliCommand> localCodeToCommandMap = Map.of(CODE, stubCliCommand());
        int cliInstanceId = engine.addCliInstance(mock(ApplicationServer.class), localCodeToCommandMap);
        sleepFor(THREAD_SWITCH_TIME);
        reset(synchronizer);

        engine.removeCliInstance(mock(ApplicationServer.class), cliInstanceId, localCodeToCommandMap);
        sleepFor(THREAD_SWITCH_TIME);
        Boolean running = engine.isRunning();

        verify(scanner, never()).cancel();
        assertThat(executorService.isTerminated()).isFalse();
        assertThat(running).isFalse();
    }

    @Test
    void lifecycleOfMultipleCliInstance() {
        Map<String, CliCommand> localCodeOfCommandMap = Map.of(CODE, stubCliCommand());
        int cliInstanceId1 = engine.addCliInstance(mock(ApplicationServer.class), localCodeOfCommandMap);
        sleepFor(THREAD_SWITCH_TIME);
        int cliInstanceId2 = engine.addCliInstance(mock(ApplicationServer.class), localCodeOfCommandMap);
        sleepFor(THREAD_SWITCH_TIME);
        boolean running = engine.isRunning();
        engine.removeCliInstance(mock(ApplicationServer.class), cliInstanceId1, localCodeOfCommandMap);
        sleepFor(THREAD_SWITCH_TIME);
        boolean runningAfterRemovalOfOneInstance = engine.isRunning();
        engine.removeCliInstance(mock(ApplicationServer.class), cliInstanceId2, localCodeOfCommandMap);
        sleepFor(THREAD_SWITCH_TIME);
        boolean runningAfterRemovalOfAllInstances = engine.isRunning();

        InOrder order = inOrder(scanner);
        order.verify(scanner).scanForCommandCode();
        order.verify(scanner).cancel();
        order.verifyNoMoreInteractions();
        assertThat(executorService.isTerminated()).isTrue();
        assertThat(cliInstanceId1).isZero();
        assertThat(cliInstanceId2).isOne();
        assertThat(running).isTrue();
        assertThat(runningAfterRemovalOfOneInstance).isTrue();
        assertThat(runningAfterRemovalOfAllInstances).isFalse();
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

    private static CliCommand stubCliCommand() {
        CliCommand result = mock(CliCommand.class);
        when(result.getCode()).thenReturn(CODE);
        return result;
    }
}
