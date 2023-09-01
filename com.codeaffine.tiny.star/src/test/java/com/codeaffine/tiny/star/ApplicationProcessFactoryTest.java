/**
 * <p>Copyright (c) 2022-2023 CA Code Affine GmbH (<a href="https://codeaffine.com">codeaffine.com</a>)</p>
 * <p>All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * <a href="https://www.eclipse.org/legal/epl-v20.html">https://www.eclipse.org/legal/epl-v20.html</a></p>
 */
package com.codeaffine.tiny.star;

import com.codeaffine.tiny.star.spi.LoggingFrameworkControl;
import com.codeaffine.tiny.star.spi.Server;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static com.codeaffine.tiny.shared.IoUtils.deleteDirectory;
import static com.codeaffine.tiny.star.ApplicationServer.DEFAULT_APPLICATION_IDENTIFIER;
import static com.codeaffine.tiny.star.ApplicationServer.State.HALTED;
import static com.codeaffine.tiny.star.ApplicationServer.newApplicationServerBuilder;
import static com.codeaffine.tiny.star.ApplicationServerTestContext.CURRENT_SERVER;
import static com.codeaffine.tiny.star.Texts.INFO_SERVER_USAGE;
import static com.codeaffine.tiny.star.Texts.INFO_WORKING_DIRECTORY;
import static java.lang.System.getProperty;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ApplicationProcessFactoryTest {

    public static final String APPLICATION_IDENTIFIER = "applicationIdentifier";
    public static final String SERVER_NAME = "serverName";

    private ApplicationConfiguration applicationConfiguration;
    private File workingDirectory;
    private Logger logger;
    @TempDir
    private File tempDir;

    @BeforeEach
    void setUp() {
        applicationConfiguration = application -> {};
        logger = mock(Logger.class);
    }

    @AfterEach
    void tearDown() {
        if (nonNull(workingDirectory) && workingDirectory.exists()) {
            deleteDirectory(workingDirectory);
        }
    }

    @Test
    void createProcess() {
        ApplicationServer applicationServer = newApplicationServerBuilder(applicationConfiguration)
            .build();
        ApplicationProcessFactory factory = new ApplicationProcessFactory(applicationServer, logger);

        ApplicationProcess actual = factory.createProcess();

        workingDirectory = new File(getProperty(applicationServer.getWorkingDirectorSystemProperty()));
        ArgumentCaptor<String> applicationIdentifierCaptor = forClass(String.class);
        InOrder order = inOrder(logger);
        order.verify(logger).info(INFO_WORKING_DIRECTORY, getProperty(applicationServer.getWorkingDirectorSystemProperty()));
        order.verify(logger).info(eq(INFO_SERVER_USAGE), applicationIdentifierCaptor.capture(), eq(CURRENT_SERVER.get().getName()));
        order.verifyNoMoreInteractions();
        assertThat(applicationIdentifierCaptor.getAllValues())
            .allMatch(DEFAULT_APPLICATION_IDENTIFIER::equals);
        assertThat(workingDirectory)
            .exists()
            .isDirectory();
        assertThat(actual.getState())
            .isSameAs(HALTED);
    }

    @Test
    @SuppressWarnings("unchecked")
    void createProcessInternals() {
        workingDirectory = new File(tempDir,"workingDirectory");
        boolean successCreatingWorkingDir = workingDirectory.mkdir();
        ApplicationServer applicationServer = newApplicationServerBuilder(applicationConfiguration, APPLICATION_IDENTIFIER)
            .build();
        WorkingDirectoryPreparer workingDirectoryPreparer = fakeWorkingDirectoryPreparer(workingDirectory);
        LoggingFrameworkControl loggingFrameworkControl = mock(LoggingFrameworkControl.class);
        LoggingFrameworkConfigurator loggingFrameworkConfigurator = fakeLoggingFrameworkConfigurator(loggingFrameworkControl);
        Server server = fakeServer();
        DelegatingServerFactory delegatingServerFactory = fakeDelegatingServerFactory(workingDirectory, server);
        Terminator terminator = mock(Terminator.class);
        TerminatorFactory terminatorFactory = fakeTerminatorFactory(loggingFrameworkControl, server, terminator);
        ShutdownHookHandler shutdownHookHandler = mock(ShutdownHookHandler.class);
        BiConsumer<ApplicationServer, ApplicationProcess> lifecycleListenerRegistrar = mock(BiConsumer.class);
        AtomicReference<Runnable> shutdownHookOperation = new AtomicReference<>();
        StartInfoPrinter startInfoPrinter = mock(StartInfoPrinter.class);
        Logger logger = mock(Logger.class);
        ApplicationProcessFactory applicationProcessFactory = new ApplicationProcessFactory(
            applicationServer,
            workingDirectoryPreparer,
            loggingFrameworkConfigurator,
            delegatingServerFactory,
            terminatorFactory,
            shutdownHookHandler,
            shutdownHookOperation,
            lifecycleListenerRegistrar,
            startInfoPrinter,
            logger
        );

        ApplicationProcess actual = applicationProcessFactory.createProcess();

        ArgumentCaptor<Runnable> shutdownHookCaptor = captureShutdownHookOperation(loggingFrameworkControl, server, terminatorFactory, shutdownHookHandler);
        shutdownHookCaptor.getValue().run();
        verify(terminator).deleteWorkingDirectory();
        verify(lifecycleListenerRegistrar).accept(applicationServer, actual);
        verify(startInfoPrinter).printStartText();
        verify(logger).info(INFO_WORKING_DIRECTORY, workingDirectory.getAbsolutePath());
        verify(logger).info(INFO_SERVER_USAGE, APPLICATION_IDENTIFIER, SERVER_NAME);
        assertThat(successCreatingWorkingDir).isTrue();
        assertThat(actual).isNotNull();
        assertThat(shutdownHookOperation).hasValue(shutdownHookCaptor.getValue());
    }

    @Test
    void registerLifeCycleListeners() {
        Object lifecycleListener = new Object();
        ApplicationServer applicationServer = newApplicationServerBuilder(application -> {})
            .withLifecycleListener(lifecycleListener)
            .build();
        ApplicationProcess process = mock(ApplicationProcess.class);

        ApplicationProcessFactory.LIFECYCLE_LISTENER_REGISTRAR.accept(applicationServer, process);

        verify(process).registerLifecycleListener(lifecycleListener);
    }

    private ArgumentCaptor<Runnable> captureShutdownHookOperation(
        LoggingFrameworkControl loggingFrameworkControl,
        Server server,
        TerminatorFactory terminatorFactory,
        ShutdownHookHandler shutdownHookHandler)
    {
        ArgumentCaptor<Runnable> shutdownHookRemoverCaptor = forClass(Runnable.class);
        verify(terminatorFactory).create(eq(workingDirectory), eq(server), eq(loggingFrameworkControl), shutdownHookRemoverCaptor.capture());
        shutdownHookRemoverCaptor.getValue().run();
        ArgumentCaptor<Runnable> result = forClass(Runnable.class);
        verify(shutdownHookHandler).deregister(result.capture());
        return result;
    }

    private static WorkingDirectoryPreparer fakeWorkingDirectoryPreparer(File workingDirectory) {
        WorkingDirectoryPreparer result = mock(WorkingDirectoryPreparer.class);
        when(result.prepareWorkingDirectory()).thenReturn(workingDirectory);
        return result;
    }

    private static LoggingFrameworkConfigurator fakeLoggingFrameworkConfigurator(LoggingFrameworkControl control) {
        LoggingFrameworkConfigurator result = mock(LoggingFrameworkConfigurator.class);
        when(result.configureLoggingFramework()).thenReturn(control);
        return result;
    }

    private static Server fakeServer() {
        Server result = mock(Server.class);
        when(result.getName()).thenReturn(SERVER_NAME);
        return result;
    }

    private static DelegatingServerFactory fakeDelegatingServerFactory(File workingDirectory, Server server) {
        DelegatingServerFactory result = mock(DelegatingServerFactory.class);
        when(result.create(workingDirectory)).thenReturn(server);
        return result;
    }

    private TerminatorFactory fakeTerminatorFactory(LoggingFrameworkControl loggingFrameworkControl, Server server, Terminator terminator) {
        TerminatorFactory result = mock(TerminatorFactory.class);
        when(result.create(eq(workingDirectory), eq(server), eq(loggingFrameworkControl), any())).thenReturn(terminator);
        return result;
    }
}
