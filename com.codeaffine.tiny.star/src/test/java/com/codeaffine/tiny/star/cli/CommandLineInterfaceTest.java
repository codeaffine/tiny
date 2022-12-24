package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.star.ApplicationServer;
import com.codeaffine.tiny.star.SystemInSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.codeaffine.tiny.star.ThreadTestHelper.sleepFor;
import static com.codeaffine.tiny.star.cli.CancelableInputStream.SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS;
import static com.codeaffine.tiny.star.cli.ExecutorServiceAdapter.TIMEOUT_AWAITING_TERMINATION;
import static java.lang.String.format;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

class CommandLineInterfaceTest {

    private static final String APPLICATION_IDENTIFIER = "applicationIdentifier";

    private DelegatingCliCommandProvider commandProvider;
    private CommandLineInterface commandLineInterface;
    private ApplicationServer applicationServer;
    private ExecutorService executor;
    private TestCliCommand command;
    private Logger logger;

    @BeforeEach
    void setUp() {
        applicationServer = stubApplicationInstance();
        commandProvider = mock(DelegatingCliCommandProvider.class);
        logger = mock(Logger.class);
        Supplier<ExecutorServiceAdapter> executorServiceAdapterFactory = () -> {
            executor = newCachedThreadPool();
            return new ExecutorServiceAdapter(executor);
        };
        CommandLineInterface.GLOBAL_ENGINE.set(new EngineFactory(executorServiceAdapterFactory).createEngine());
        commandLineInterface = new CommandLineInterface(commandProvider, new AtomicReference<>(), logger);
        command = new TestCliCommand();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        commandLineInterface.stopCli();
        boolean isTerminated = executor.awaitTermination(TIMEOUT_AWAITING_TERMINATION, MILLISECONDS);
        assertThat(isTerminated).isTrue();
        assertThat(CommandLineInterface.GLOBAL_ENGINE).hasValue(null);
    }

    @Test
    void startCli() {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);

        verify(logger).info(command.getDescription(command, applicationServer));
    }

    @Test
    void startCliMoreThanOnce() {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);
        commandLineInterface.startCli(applicationServer);

        verify(logger).info(command.getDescription(command, applicationServer));
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    void handleCommandRequest(SystemInSupplier systemInSupplier) throws IOException {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);
        systemInSupplier.getSupplierOutputStream().write(format("%s%n", command.getCode()).getBytes());
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);

        verify(applicationServer).stop();
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    void stopCli(SystemInSupplier systemInSupplier) throws IOException {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);
        commandLineInterface.stopCli();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        systemInSupplier.getSupplierOutputStream().write(format("%s%n", command.getCode()).getBytes());

        verify(applicationServer, never()).stop();
        verify(logger).info(command.getDescription(command, applicationServer));
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    void stopCliMoreThanOnce(SystemInSupplier systemInSupplier) throws IOException {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        commandLineInterface.stopCli();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        Throwable actual = catchThrowable(() -> commandLineInterface.stopCli());
        systemInSupplier.getSupplierOutputStream().write(format("%s%n", command.getCode()).getBytes());

        assertThat(actual).isNull();
        verify(applicationServer, never()).stop();
        verify(logger).info(command.getDescription(command, applicationServer));
    }

    @Test
    void restartCli() throws InterruptedException {
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        commandLineInterface.stopCli();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        boolean isTerminated = executor.awaitTermination(100, MILLISECONDS);
        commandLineInterface.startCli(applicationServer);

        verify(logger, times(2)).info(command.getDescription(command, applicationServer));
        assertThat(isTerminated).isTrue();
    }

    private void stubDelegatingCliCommandProvider(TestCliCommand... commands) {
        when(commandProvider.getCliCommands()).thenReturn(Set.of(commands));
    }

    private static ApplicationServer stubApplicationInstance() {
        ApplicationServer result = mock(ApplicationServer.class);
        when(result.getIdentifier()).thenReturn(APPLICATION_IDENTIFIER);
        return result;
    }
}
