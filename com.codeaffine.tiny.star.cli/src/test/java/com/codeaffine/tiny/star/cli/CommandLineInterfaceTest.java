package com.codeaffine.tiny.star.cli;

import com.codeaffine.tiny.test.SystemInSupplier;
import com.codeaffine.tiny.star.ApplicationServer;
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

import static com.codeaffine.tiny.star.cli.CancelableInputStream.SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS;
import static com.codeaffine.tiny.star.cli.ExecutorServiceAdapter.TIMEOUT_AWAITING_TERMINATION;
import static com.codeaffine.tiny.shared.Threads.sleepFor;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.*;
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
        command = new TestCliCommand();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if(nonNull(commandLineInterface)) {
            commandLineInterface.stopCli();
        }
        if(nonNull(executor)) {
            boolean isTerminated = executor.awaitTermination(TIMEOUT_AWAITING_TERMINATION, MILLISECONDS);
            assertThat(isTerminated).isTrue();
        }
        assertThat(CommandLineInterface.GLOBAL_ENGINE).hasValue(null);
    }

    @Test
    void startCli() {
        setUpTestContext();
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);

        verify(logger).info(command.getDescription(command, applicationServer));
    }

    @Test
    void startCliMoreThanOnce() {
        setUpTestContext();
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);
        commandLineInterface.startCli(applicationServer);

        verify(logger).info(command.getDescription(command, applicationServer));
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    @SuppressWarnings("JUnitMalformedDeclaration")
    void handleCommandRequest(SystemInSupplier systemInSupplier) throws IOException {
        setUpTestContext();
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);
        triggerCommand(systemInSupplier, command.getCode());
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);

        verify(applicationServer).stop();
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    @SuppressWarnings("JUnitMalformedDeclaration")
    void stopCli(SystemInSupplier systemInSupplier) throws IOException {
        setUpTestContext();
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);
        commandLineInterface.stopCli();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        triggerCommand(systemInSupplier, command.getCode());

        verify(applicationServer, never()).stop();
        verify(logger).info(command.getDescription(command, applicationServer));
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    @SuppressWarnings("JUnitMalformedDeclaration")
    void stopCliMoreThanOnce(SystemInSupplier systemInSupplier) throws IOException {
        setUpTestContext();
        stubDelegatingCliCommandProvider(command);

        commandLineInterface.startCli(applicationServer);
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        commandLineInterface.stopCli();
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        Exception actual = catchException(() -> commandLineInterface.stopCli());
        triggerCommand(systemInSupplier, command.getCode());

        assertThat(actual).isNull();
        verify(applicationServer, never()).stop();
        verify(logger).info(command.getDescription(command, applicationServer));
    }

    @Test
    void restartCli() throws InterruptedException {
        setUpTestContext();
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

    @Test
    @ExtendWith(SystemInSupplier.class)
    @SuppressWarnings("JUnitMalformedDeclaration")
    void lifeCycleOfMultipleCliInstances(SystemInSupplier systemInSupplier) throws IOException {
        setUpTestContext();
        stubDelegatingCliCommandProvider(command);
        ApplicationServer otherApplicationServer = mock(ApplicationServer.class);
        CommandLineInterface otherCommandLineInterface = new CommandLineInterface(commandProvider, new AtomicReference<>(), logger);
        CliCommandAdapter commandAdapter = new CliCommandAdapter(applicationServer, command, 1);

        commandLineInterface.startCli(applicationServer);
        otherCommandLineInterface.startCli(otherApplicationServer);
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        triggerCommand(systemInSupplier, command.getCode());
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        triggerCommand(systemInSupplier, commandAdapter.getCode());
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        commandLineInterface.stopCli();
        Engine afterStoppingFirst = CommandLineInterface.GLOBAL_ENGINE.get();
        otherCommandLineInterface.stopCli();
        Engine afterStoppingSecond = CommandLineInterface.GLOBAL_ENGINE.get();

        verify(logger).info(command.getDescription(command, applicationServer));
        verify(logger).info(commandAdapter.getDescription(commandAdapter, otherApplicationServer));
        verify(applicationServer).stop();
        verify(otherApplicationServer).stop();
        assertThat(afterStoppingFirst).isNotNull();
        assertThat(afterStoppingSecond).isNull();
    }

    @Test
    @ExtendWith(SystemInSupplier.class)
    @SuppressWarnings("JUnitMalformedDeclaration")
    void lifeCycleCommandLineInterfaceWithCustomCliCommandProvider(SystemInSupplier systemInSupplier) throws IOException {
        commandLineInterface = new CommandLineInterface(() -> Set.of(command));

        CommandLineInterface.GLOBAL_ENGINE.set(null);
        commandLineInterface.startCli(applicationServer);
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        triggerCommand(systemInSupplier, command.getCode());
        sleepFor(SUSPENDED_TIME_IN_MILLIS_BETWEEN_DATA_AVAILABILITY_CHECKS * 2);
        Engine beforeStopping = CommandLineInterface.GLOBAL_ENGINE.get();
        commandLineInterface.stopCli();
        Engine afterStopping = CommandLineInterface.GLOBAL_ENGINE.get();

        verify(applicationServer).stop();
        assertThat(beforeStopping).isNotNull();
        assertThat(afterStopping).isNull();
    }

    @Test
    void constructorWithNullAsCommandProviderArgument() {
        assertThatThrownBy(() -> new CommandLineInterface(null))
                .isInstanceOf(NullPointerException.class);
    }

    private static ApplicationServer stubApplicationInstance() {
        ApplicationServer result = mock(ApplicationServer.class);
        when(result.getIdentifier()).thenReturn(APPLICATION_IDENTIFIER);
        return result;
    }

    private void setUpTestContext() {
        Supplier<ExecutorServiceAdapter> executorServiceAdapterFactory = () -> {
            executor = newCachedThreadPool();
            return new ExecutorServiceAdapter(executor);
        };
        CommandLineInterface.GLOBAL_ENGINE.set(new EngineFactory(executorServiceAdapterFactory).createEngine());
        commandLineInterface = new CommandLineInterface(commandProvider, new AtomicReference<>(), logger);
    }

    private void stubDelegatingCliCommandProvider(TestCliCommand... commands) {
        when(commandProvider.getCliCommands()).thenReturn(Set.of(commands));
    }

    private static void triggerCommand(SystemInSupplier systemInSupplier, String code) throws IOException {
        systemInSupplier.getSupplierOutputStream().write(format("%s%n", code).getBytes());
    }
}
