package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.SystemInSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static com.codeaffine.tiny.star.ThreadTestHelper.sleepFor;
import static com.codeaffine.tiny.star.cli.CancelableInputStream.SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS;
import static com.codeaffine.tiny.star.cli.Texts.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class InputScannerTest {

    private static final String COMMAND_LINE = "commandLine\n";

    private CommandDispatcher commandDispatcher;
    private ExecutorService executorService;
    private InputScanner scanner;
    private Logger logger;

    @BeforeEach
    void setUp() {
        executorService = newSingleThreadExecutor();
        commandDispatcher = mock(CommandDispatcher.class);
        logger = mock(Logger.class);
        scanner = new InputScanner(commandDispatcher, logger);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        scanner.cancel();
        executorService.shutdownNow();
        boolean isTerminated = executorService.awaitTermination(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS, MILLISECONDS);
        assertThat(isTerminated).isTrue();
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    @SuppressWarnings("JUnitMalformedDeclaration")
    void scanForCommandCode(SystemInSupplier systemInSupplier) throws IOException {
        executorService.execute(() -> scanner.scanForCommandCode());
        systemInSupplier.getSupplierOutputStream().write(COMMAND_LINE.getBytes(UTF_8));

        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS);
        InOrder order = inOrder(commandDispatcher, logger);
        order.verify(logger).debug(DEBUG_START_SCANNING_FOR_COMMANDS);
        order.verify(logger).debug(DEBUG_DISPATCHING_COMMAND, COMMAND_LINE.trim());
        order.verify(commandDispatcher).dispatchCommand(COMMAND_LINE.trim());
        order.verifyNoMoreInteractions();
    }

    @Test
    void scanForCommandCodeOnInterrupt() {
        executorService.execute(() -> scanner.scanForCommandCode());
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS);

        executorService.shutdownNow();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS);

        InOrder order = inOrder(logger);
        order.verify(logger).debug(DEBUG_START_SCANNING_FOR_COMMANDS);
        order.verify(logger).debug(DEBUG_END_SCANNING_FOR_COMMANDS);
        order.verifyNoMoreInteractions();
        verify(commandDispatcher, never()).dispatchCommand(any());
    }

    @Test
    void scanForCommandCodeOnCancel() {
        executorService.execute(() -> scanner.scanForCommandCode());
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS);

        scanner.cancel();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS);

        InOrder order = inOrder(logger);
        order.verify(logger).debug(DEBUG_START_SCANNING_FOR_COMMANDS);
        order.verify(logger).debug(DEBUG_END_SCANNING_FOR_COMMANDS);
        order.verifyNoMoreInteractions();
        verify(commandDispatcher, never()).dispatchCommand(any());
    }

    @Test
    void scanForCommandCodeOnMultipleCancelCalls() {
        executorService.execute(() -> scanner.scanForCommandCode());
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS);

        scanner.cancel();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS);
        scanner.cancel();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS);

        InOrder order = inOrder(logger);
        order.verify(logger).debug(DEBUG_START_SCANNING_FOR_COMMANDS);
        order.verify(logger).debug(DEBUG_END_SCANNING_FOR_COMMANDS);
        order.verifyNoMoreInteractions();
        verify(commandDispatcher, never()).dispatchCommand(any());
    }

    @Test
    void constructWithNullAsCommandDispatcherArgument() {
        assertThatThrownBy(() -> new InputScanner(null))
            .isInstanceOf(NullPointerException.class);
    }
}
