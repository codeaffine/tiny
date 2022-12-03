package com.codeaffine.tiny.star.cli;

import static com.codeaffine.tiny.star.ThreadTestHelper.sleepFor;
import static com.codeaffine.tiny.star.cli.CancelableInputStream.SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static java.lang.String.format;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;

import com.codeaffine.tiny.star.ApplicationInstance;
import com.codeaffine.tiny.star.SystemInSupplier;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

class CommandLineInterfaceTest {

    private static final String APPLICATION_IDENTIFIER = "applicationIdentifier";

    private DelegatingCliCommandProvider commandProvider;
    private CommandLineInterface commandLineInterface;
    private ApplicationInstance applicationInstance;
    private ExecutorService executor;
    private TestCliCommand command;
    private Logger logger;

    @BeforeEach
    void setUp() {
        applicationInstance = stubApplicationInstance();
        commandProvider = mock(DelegatingCliCommandProvider.class);
        logger = mock(Logger.class);
        Supplier<ExecutorServiceAdapter> executorServiceAdapterFactory = () -> {
            executor = newCachedThreadPool();
            return new ExecutorServiceAdapter(executor);
        };
        commandLineInterface = new CommandLineInterface(commandProvider, executorServiceAdapterFactory, new AtomicReference<>(), logger);
        command = new TestCliCommand();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        commandLineInterface.stopCli();
        boolean isTerminated = executor.awaitTermination(ExecutorServiceAdapter.TIMEOUT_AWAITING_TERMINATION, MILLISECONDS);
        assertThat(isTerminated).isTrue();
    }

    @Test
    void startCli() {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationInstance);

        verify(logger).info(command.getDescription(command, applicationInstance));
    }

    @Test
    void startCliMoreThanOnce() {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationInstance);
        commandLineInterface.startCli(applicationInstance);

        verify(logger).info(command.getDescription(command, applicationInstance));
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    void handleCommandRequest(SystemInSupplier systemInSupplier) throws IOException {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationInstance);
        systemInSupplier.getSupplierOutputStream().write(format("%s%n", command.getCode()).getBytes());
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);

        verify(applicationInstance).stop();
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    void stopCli(SystemInSupplier systemInSupplier) throws IOException {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationInstance);
        commandLineInterface.stopCli();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        systemInSupplier.getSupplierOutputStream().write(format("%s%n", command.getCode()).getBytes());

        verify(applicationInstance, never()).stop();
        verify(logger).info(command.getDescription(command, applicationInstance));
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    void stopCliMoreThanOnce(SystemInSupplier systemInSupplier) throws IOException {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationInstance);
        commandLineInterface.stopCli();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        Throwable actual = catchThrowable(() -> commandLineInterface.stopCli());
        systemInSupplier.getSupplierOutputStream().write(format("%s%n", command.getCode()).getBytes());

        assertThat(actual).isNull();
        verify(applicationInstance, never()).stop();
        verify(logger).info(command.getDescription(command, applicationInstance));
    }

    @Test
    void restartCli() throws InterruptedException {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationInstance);
        commandLineInterface.stopCli();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        boolean isTerminated = executor.awaitTermination(100, MILLISECONDS);
        commandLineInterface.startCli(applicationInstance);

        verify(logger, times(2)).info(command.getDescription(command, applicationInstance));
    }

    private void stubDelegatingCliCommandProvider(TestCliCommand... commands) {
        when(commandProvider.getCliCommands()).thenReturn(Set.of(commands));
    }

    private static ApplicationInstance stubApplicationInstance() {
        ApplicationInstance result = mock(ApplicationInstance.class);
        when(result.getIdentifier()).thenReturn(APPLICATION_IDENTIFIER);
        return result;
    }
}
